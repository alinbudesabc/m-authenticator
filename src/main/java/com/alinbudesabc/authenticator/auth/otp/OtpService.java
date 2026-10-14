package com.alinbudesabc.authenticator.auth.otp;

import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {


    private final Map<String, OtpMessagingStrategy> messagingStrategies;

    public Pair<Boolean, String> handleAuthentication(UserDetailsDto userDetail, String otpCode) {
        if(!userDetail.getTwoFactorEnabled()){
            return Pair.of(false, "Two factor authentication is not enabled in db for this user");
        }
        OtpMessagingStrategy tfaStrategy = messagingStrategies.get(userDetail.getTwoFactorMessageStrategy().getLabel());
        if(MessagingStrategyEnum.AUTHENTICATOR == tfaStrategy.getType()){
            return ((OtpGoogleAuthenticatorStrategy) tfaStrategy).validateCode(userDetail.getMfaSecret(), otpCode) ?  Pair.of(true, "Code validated") : Pair.of(false, "Code not valid");
        }
        return Pair.of(false, "Error in tfa flow");
    }
}
