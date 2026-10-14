package com.alinbudesabc.authenticator.model.dto;

import lombok.Builder;

@Builder
public record UserDto(long id, String username, String email){
}
