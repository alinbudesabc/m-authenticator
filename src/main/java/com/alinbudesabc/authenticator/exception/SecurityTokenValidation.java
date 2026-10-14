package com.alinbudesabc.authenticator.exception;

import jakarta.servlet.ServletException;

public class SecurityTokenValidation extends ServletException {

    public SecurityTokenValidation() {
    }

    public SecurityTokenValidation(String message) {
        super(message);
    }
}
