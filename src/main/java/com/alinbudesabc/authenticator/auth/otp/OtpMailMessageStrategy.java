package com.alinbudesabc.authenticator.auth.otp;


import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service("email")
public class OtpMailMessageStrategy implements OtpMessagingStrategy{


    @Override
    public boolean validateCode(String key, String code) {
        throw new NotImplementedException("Need to implement it");
    }

    @Override
    public MessagingStrategyEnum getType() {
        return null;
    }
}
