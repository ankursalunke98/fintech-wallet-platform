package com.ankur.userservice.exception;

public class IdempotencyConflictException extends RuntimeException{
    public IdempotencyConflictException(String idempotencyKey){
        super("Idempotency key '" + idempotencyKey +"' was used previously with different request");
    }
}
