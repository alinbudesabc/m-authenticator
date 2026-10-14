package com.alinbudesabc.authenticator.exception;

public class BlockedUserException extends RuntimeException{
    public BlockedUserException(String message) {
        super(message);
    }
}
