package com.alinbudesabc.authenticator.dto;

import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import lombok.Builder;

//@Getter
@Builder
public record UserDetailsDtoImpl(
        String username,
        String email,
        Boolean twoFactorEnabled,
        MessagingStrategyEnum twoFactorMessageStrategy,
        AuthTypeEnum authStrategy,
        String mfaSecret,
        boolean isAccountNonLocked
) implements UserDetailsDto {
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    @Override
    public MessagingStrategyEnum getTwoFactorMessageStrategy() {
        return twoFactorMessageStrategy;
    }

    @Override
    public AuthTypeEnum getAuthStrategy() {
        return authStrategy;
    }

    @Override
    public String getMfaSecret() {
        return mfaSecret;
    }

    @Override
    public boolean getIsAccountNonLocked() {
        return true;
    }
}
