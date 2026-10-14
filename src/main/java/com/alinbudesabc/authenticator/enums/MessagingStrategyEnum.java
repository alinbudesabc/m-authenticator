package com.alinbudesabc.authenticator.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagingStrategyEnum {
    EMAIL("email"),
    SMS("sms"),
    AUTHENTICATOR("authenticator"),
    NONE("none");

    private final String label;
}
