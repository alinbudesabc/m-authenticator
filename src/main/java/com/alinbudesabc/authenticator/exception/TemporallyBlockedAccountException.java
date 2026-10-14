package com.alinbudesabc.authenticator.exception;

public class TemporallyBlockedAccountException extends RuntimeException{
    public TemporallyBlockedAccountException(String message) {
        super(message);
    }
}
