package com.alinbudesabc.authenticator.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String label;
}
