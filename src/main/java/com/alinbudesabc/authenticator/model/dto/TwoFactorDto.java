package com.alinbudesabc.authenticator.model.dto;

import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import lombok.Builder;

@Builder
public record TwoFactorDto(
        String username,
        String secretTfaKey,
        boolean twoFactorEnabled,
        AuthTypeEnum authTypeEnum,
        MessagingStrategyEnum messagingStrategyEnum
) {
}
