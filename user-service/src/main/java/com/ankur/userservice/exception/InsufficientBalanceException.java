package com.ankur.userservice.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(Long accountId, BigDecimal available, BigDecimal request){
        super(String.format("Insufficient balance in account %d: available=%s, requested=%s", accountId, available, request));
    }
}
