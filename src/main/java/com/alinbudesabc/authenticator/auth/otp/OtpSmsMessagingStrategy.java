package com.alinbudesabc.authenticator.auth.otp;


import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component("sms")
public class OtpSmsMessagingStrategy implements OtpMessagingStrategy{


    @Override
    public boolean validateCode(String key, String code) {
        throw new NotImplementedException("Need to be implemented");
    }

    @Override
    public MessagingStrategyEnum getType() {
        return null;
    }
}
