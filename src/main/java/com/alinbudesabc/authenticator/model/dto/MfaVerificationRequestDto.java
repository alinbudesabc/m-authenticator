package com.alinbudesabc.authenticator.model.dto;

public record MfaVerificationRequestDto(String username, String tfaCode) {
}
