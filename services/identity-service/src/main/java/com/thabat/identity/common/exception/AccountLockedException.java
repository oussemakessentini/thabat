package com.thabat.identity.common.exception;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("This account is locked");
    }
}
