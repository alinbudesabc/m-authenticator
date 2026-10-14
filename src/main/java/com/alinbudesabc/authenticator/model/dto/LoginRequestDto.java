package com.alinbudesabc.authenticator.model.dto;

import lombok.Builder;

@Builder
public record LoginRequestDto(String username, String password) {
}
