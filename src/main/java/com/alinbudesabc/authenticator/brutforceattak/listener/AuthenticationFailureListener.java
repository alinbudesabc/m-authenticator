package com.alinbudesabc.authenticator.brutforceattak.listener;


import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event){
        //send it to rabbit queue


//        Object principal = event.getAuthentication().getPrincipal();
//        if(principal instanceof String username){
//            loginAttemptService.addUserToLoginAttemptCache(username);
//            throw new BadCredentialsException(event.getException().getMessage());
//        }
    }
}
