package com.alinbudesabc.authenticator.model.dto;

public record LoginResponseDto(
    Integer status, 
    String jwtToken, 
    String username) {
}
