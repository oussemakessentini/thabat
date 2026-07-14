package com.thabat.identity.common.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("This account is disabled");
    }
}
