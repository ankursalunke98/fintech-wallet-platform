package com.ankur.userservice.controller;

import com.ankur.userservice.dto.*;
import com.ankur.userservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request){
        log.info("Received createWallet request for user_id: {} ", request.getUserId());
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<DepositResponse> depositAmount(@PathVariable Long accountId, @Valid @RequestBody DepositRequest request){
        log.info("Received a deposit request for Account: {}", accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.deposit(accountId, request.getAmount()));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long accountId){
        log.info("Received get balance request for account_id: {}", accountId);
        BalanceResponse response = walletService.getBalance(accountId);
        return ResponseEntity.ok(response);
    }
}
