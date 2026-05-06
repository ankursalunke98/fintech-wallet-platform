package com.ankur.userservice.service;
import com.ankur.userservice.dto.BalanceResponse;
import com.ankur.userservice.dto.CreateWalletRequest;
import com.ankur.userservice.dto.WalletResponse;
import com.ankur.userservice.entity.*;
import com.ankur.userservice.exception.AccountNotFoundException;
import com.ankur.userservice.exception.UserNotFoundException;
import com.ankur.userservice.repository.AccountRepository;
import com.ankur.userservice.repository.LedgerEntryRepository;
import com.ankur.userservice.repository.TransactionRepository;
import com.ankur.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request){
        log.info("Creating wallet for user_id: {}", request.getUserId());
        if (!userRepository.existsById(request.getUserId())){
            throw new UserNotFoundException(request.getUserId());
        }

        Account userAccount = Account.builder()
                .userId(request.getUserId())
                .accountType(AccountType.USER)
                .currency("INR")
                .isActive(true)
                .build();

        userAccount = accountRepository.save(userAccount);

        log.info("Created user account with id: {}", userAccount.getUserId());

        BigDecimal balance = BigDecimal.ZERO;
        if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0){
            balance = createInitialDeposit(userAccount, request.getInitialDeposit());
        }

        return WalletResponse.builder()
                .accountId(userAccount.getId())
                .userId(userAccount.getUserId())
                .accountType(userAccount.getAccountType())
                .currency(userAccount.getCurrency())
                .balance(balance)
                .createdAt(userAccount.getCreatedAt())
                .build();
    }

    private BigDecimal createInitialDeposit(Account userAccount, BigDecimal amount){
        log.info("Creating initial deposit of {} for the account {}", amount, userAccount.getId());

        Account systemDepositAccount = getOrCreateSystemAccount(AccountType.SYSTEM_DEPOSIT);

        Transaction transaction = Transaction.builder()
                .transactionRef("TXN-" + UUID.randomUUID().toString())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .description("Initial deposit on wallet creation")
                .build();
        transaction = transactionRepository.save(transaction);

        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(systemDepositAccount.getId())
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .currency("INR")
                .build();

        ledgerEntryRepository.save(debitEntry);

        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(userAccount.getId())
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .currency("INR")
                .build();
        ledgerEntryRepository.save(creditEntry);

        log.info("Initial deposit complete. Transaction: {} debit: {} credit: {}", transaction.getId(), debitEntry.getId(), creditEntry.getId());

        return amount;
    }
    private Account getOrCreateSystemAccount(AccountType accountType){
        List<Account> existing = accountRepository.findByUserId(null);
        for (Account account : existing){
            if (account.getAccountType() == accountType){
                return account;
            }
        }

        log.info("Creating a system account: {}", accountType);
        Account systemAccount = Account.builder()
                .userId(null)
                .accountType(accountType)
                .currency("INR")
                .isActive(true)
                .build();
        return accountRepository.save(systemAccount);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId){
        log.info("Fetching account balance fot the account: {}", accountId);

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountId(accountId);
        BigDecimal balance = entries.stream().map(entry -> entry.getEntryType() == EntryType.CREDIT
                ? entry.getAmount() : entry.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Account {} balance computed: {}", accountId, balance);

        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .currency(account.getCurrency())
                .build();
    }

}
