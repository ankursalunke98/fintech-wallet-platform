package com.ankur.userservice.service;

import com.ankur.userservice.dto.TransferRequest;
import com.ankur.userservice.dto.TransferResponse;
import com.ankur.userservice.entity.*;
import com.ankur.userservice.exception.AccountNotFoundException;
import com.ankur.userservice.exception.IdempotencyConflictException;
import com.ankur.userservice.exception.InsufficientBalanceException;
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

        // Step 1: idempotency check
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

        // Step 2: validate accounts exist
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));
        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

        // Step 3: check sender balance
        BigDecimal senderBalance = computeBalance(fromAccount.getId());
        if (senderBalance.compareTo(request.getAmount()) < 0){
            throw new InsufficientBalanceException(fromAccount.getId(), senderBalance, request.getAmount());
        }

        // Step 4: create transaction record
        Transaction transaction = Transaction.builder()
                .transactionRef("TXN-" + UUID.randomUUID().toString())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        // Step 5: create the two ledger entries (double-entry)
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

        // Step 6: trigger optimistic-lock version increment on both accounts

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Transfer complete: transactionRef={}", transaction.getTransactionRef());

        // Step 8: Build a response

        TransferResponse response = TransferResponse.builder()
                .transactionRef(transaction.getTransactionRef())
                .fromAccountId(fromAccount.getId())
                .toAccountId(toAccount.getId())
                .amount(request.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();

        // Step 8: cache the idempotency record

        IdempotencyKey idempotencyRecord = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .responseBody(serializeResponse(response))
                .responseStatus(201)
                .build();

        idempotencyKeyRepository.save(idempotencyRecord);
        return response;
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
