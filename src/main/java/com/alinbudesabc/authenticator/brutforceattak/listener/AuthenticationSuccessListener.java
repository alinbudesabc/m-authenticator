package com.alinbudesabc.authenticator.brutforceattak.listener;

import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof String username){
            loginAttemptService.evictUserFromLoginAttemptCache(username);
            loginAttemptService.getBlackListUsers().remove(username);
        }
    }
}
