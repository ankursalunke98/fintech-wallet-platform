package com.ankur.userservice.dto;

import com.ankur.userservice.entity.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class WalletResponse {
    private Long accountId;
    private Long userId;
    private AccountType accountType;
    private String currency;
    private BigDecimal balance;
    private OffsetDateTime createdAt;


}
