package com.ankur.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @DecimalMin(value = "0.0", inclusive = true, message = "initial deposit cannot be negative")
    private BigDecimal initialDeposit;

}
