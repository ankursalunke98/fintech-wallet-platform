package com.ankur.userservice.dto;

import com.ankur.userservice.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class TransferResponse {
    private String transactionRef;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private TransactionStatus status;
    private OffsetDateTime createdAt;
}
