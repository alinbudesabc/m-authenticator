package com.alinbudesabc.authenticator.model.dto.projection;

import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;

public interface UserDetailsDto {
        String getUsername();
        String getEmail();
        Boolean getTwoFactorEnabled();
        MessagingStrategyEnum getTwoFactorMessageStrategy();
        AuthTypeEnum getAuthStrategy();
        String getMfaSecret();
        boolean getIsAccountNonLocked();
}
