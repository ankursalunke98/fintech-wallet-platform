package com.ankur.userservice.service;

import com.ankur.userservice.dto.TransferRequest;
import com.ankur.userservice.dto.TransferResponse;
import com.ankur.userservice.entity.*;
import com.ankur.userservice.exception.AccountNotFoundException;
import com.ankur.userservice.exception.IdempotencyConflictException;
import com.ankur.userservice.exception.InsufficientBalanceException;
import com.ankur.userservice.exception.LedgerIntegrityException;
import com.ankur.userservice.repository.AccountRepository;
import com.ankur.userservice.repository.IdempotencyKeyRepository;
import com.ankur.userservice.repository.LedgerEntryRepository;
import com.ankur.userservice.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransferResponse executeTransfer(String idempotencyKey, TransferRequest request){
        log.info("Transfer request: from={}, to={}, amount={}, idempotencyKey={}",
                request.getFromAccountId(), request.getToAccountId(),
                request.getAmount(), idempotencyKey);

        // Hash the request body so we can detect if the same key gets reused with different params later
        String requestHash = computeRequestHash(request);
        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);

        if (existingKey.isPresent()){
            IdempotencyKey stored = existingKey.get();

            if (stored.getRequestHash().equals(requestHash)){
                log.info("Idempotency key already processed returning cached response");
                return parseResponse(stored.getResponseBody());
            }

            log.warn("Idempotency key reused with different request: {}", idempotencyKey);
            throw new IdempotencyConflictException(idempotencyKey);
        }

        // Both accounts must exist before we touch the ledger
        // (orElseThrow gives us a 404 via GlobalExceptionHandler)
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));
        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

        // Balance is derived from ledger entries — no balance column to read
        BigDecimal senderBalance = computeBalance(fromAccount.getId());
        if (senderBalance.compareTo(request.getAmount()) < 0){
            throw new InsufficientBalanceException(fromAccount.getId(), senderBalance, request.getAmount());
        }

        // Building the transaction record first so both ledger entries can refer to its id
        Transaction transaction = Transaction.builder()
                .transactionRef("TXN-" + UUID.randomUUID().toString())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        // Double-entry: debit the sender, credit the receiver. Both atomic via @Transactional
        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(fromAccount.getId())
                .entryType(EntryType.DEBIT)
                .amount(request.getAmount())
                .currency("INR")
                .build();

        ledgerEntryRepository.save(debitEntry);

        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(toAccount.getId())
                .entryType(EntryType.CREDIT)
                .amount(request.getAmount())
                .currency("INR")
                .build();
        ledgerEntryRepository.save(creditEntry);

        // validate the entries we just wrote — if integrity is broken, rollback the entire transfer
        List<LedgerEntry> entries = List.of(debitEntry, creditEntry);
        validateTransferIntegrity(transaction, entries);

        // Re-saving forces Hibernate to bump @Version on both accounts
        accountRepository.save(fromAccount);

        // Re-saving forces Hibernate to bump @Version on both accounts.
        // Throws OptimisticLockException if another transfer raced us here.
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Transfer complete: transactionRef={}", transaction.getTransactionRef());

        // Build the response we'll return to the client AND cache for retries
        TransferResponse response = TransferResponse.builder()
                .transactionRef(transaction.getTransactionRef())
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();

        // Cache the response so retries with the same idempotency key return this exact payload
        IdempotencyKey idempotencyRecord = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .responseBody(serializeResponse(response))
                .responseStatus(201)
                .build();

        idempotencyKeyRepository.save(idempotencyRecord);
        return response;

    }

    //catches class of bug where transfer ends up with malformed ledger entries
    //Yesterday's bug both debit and credit landing on the same account would have failed here
    // and roll back whole transfer instead of silently going through
    private void validateTransferIntegrity(Transaction transaction, List<LedgerEntry> entries){
        if (entries.size() != 2){
            throw new LedgerIntegrityException("Number of entries are not 2 in the transaction");
        }
        long debit = entries.stream().filter(e -> e.getEntryType() == EntryType.DEBIT).count();
        long credit = entries.stream().filter(e -> e.getEntryType() == EntryType.CREDIT).count();

        if (debit != 1 || credit != 1){
            throw new LedgerIntegrityException("Either debit or credit entries are not 1");
        }

        LedgerEntry first = entries.get(0);
        LedgerEntry second = entries.get(1);

        if (first.getAccountId().equals(second.getAccountId())){
            throw new LedgerIntegrityException("both ledger entries point to the same account");
        }

        if (first.getAmount().compareTo(second.getAmount()) != 0){
            throw new LedgerIntegrityException("the transaction debit amount doesn't match with the credit amount");
        }

        if (!first.getCurrency().equals(second.getCurrency())){
            throw new LedgerIntegrityException("the currencies for the transaction don't match");
        }

    }

    private BigDecimal computeBalance(Long accountId){
        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountId(accountId);

        return entries.stream().map(entry -> entry.getEntryType() == EntryType.CREDIT ?
                entry.getAmount() : entry.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String computeRequestHash(TransferRequest request){
        try {
            String requestStr = String.format("%d:%d:%s", request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(requestStr.getBytes());
            return HexFormat.of().formatHex(hash);
        }catch (Exception e){
            throw new RuntimeException("Failed to compute request hash", e);
        }
    }

    private String serializeResponse(TransferResponse response){
        try {
            return objectMapper.writeValueAsString(response);
        }catch (Exception e){
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    private TransferResponse parseResponse (String json){
        try {
            return objectMapper.readValue(json, TransferResponse.class);
        }catch (Exception e){
            throw new RuntimeException("Failed to parse cached response", e);
        }
    }
}
