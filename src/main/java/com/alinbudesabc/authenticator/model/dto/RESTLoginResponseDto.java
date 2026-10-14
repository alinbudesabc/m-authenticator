package com.alinbudesabc.authenticator.model.dto;

public record RESTLoginResponseDto(
    Integer status, 
    String jwtToken, 
    String refreshToken, 
    boolean tfaEnabled, 
    String message) {
}
