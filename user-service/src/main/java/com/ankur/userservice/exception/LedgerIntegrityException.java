package com.ankur.userservice.exception;

public class LedgerIntegrityException extends RuntimeException{
    public LedgerIntegrityException(String message){
        super(message);
    }
}
