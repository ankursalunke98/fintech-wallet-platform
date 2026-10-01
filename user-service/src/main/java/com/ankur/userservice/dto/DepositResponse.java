package com.ankur.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepositResponse {
    private String transactionRef;
    private Long accountId;
    private BigDecimal amount;
    private BigDecimal balance;
}
