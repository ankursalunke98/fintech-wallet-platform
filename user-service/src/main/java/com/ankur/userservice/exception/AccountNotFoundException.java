package com.ankur.userservice.exception;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(Long accountId){
        super("Account not found with user id " + accountId);
    }
}
