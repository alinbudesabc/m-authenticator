package com.alinbudesabc.authenticator.auth.otp;

import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;

public interface OtpMessagingStrategy {

    boolean validateCode(String key, String code);
    MessagingStrategyEnum getType();
}
