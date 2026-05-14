package com.ankur.userservice.controller;

import com.ankur.userservice.dto.TransferRequest;
import com.ankur.userservice.dto.TransferResponse;
import com.ankur.userservice.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/transfers")
@RestController
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     @Valid @RequestBody TransferRequest request){

        log.info("Received transfer request with idempotency key: {}", idempotencyKey);
        TransferResponse response = transferService.executeTransfer(idempotencyKey, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
