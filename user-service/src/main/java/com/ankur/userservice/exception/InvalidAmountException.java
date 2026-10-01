package com.ankur.userservice.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException(BigDecimal amount){super("The Amount entered is invalid" + amount);}
}
