package com.alinbudesabc.authenticator.config;

import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.exception.BlockedUserException;
import com.alinbudesabc.authenticator.model.dto.UserWithRolesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"prod", "dev"})
public class ProdAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final LoginAttemptService loginAttemptService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pass = authentication.getCredentials().toString();

        if(loginAttemptService.isAccountPermanentlyBlocked(username)){
            throw new BlockedUserException("Account is permanently locked.");
        }

        //TODO: create a rabbit service that should send message when user blocked, etc
        // the channel will be listened by other microservice in order to block the access to the app
        // could be a published event and in the listener it could be the logic for sending item to rabbit queue

        UserWithRolesDto user = userDetailsService.getUserWithRoles(username);

        if(passwordEncoder.matches(pass, user.getPassword())){
           loginAttemptService.handleLoginSuccess(username);
            Authentication auth = new UsernamePasswordAuthenticationToken(username, pass, user.getRoles());
            eventPublisher.publishEvent(new AuthenticationSuccessEvent(auth));
            return auth;
        }
        Authentication auth = new UsernamePasswordAuthenticationToken(username, pass, user.getRoles());
        auth.setAuthenticated(false);
        AuthenticationException exception = new BadCredentialsException("Bad credentials");
        eventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(auth, exception));
        loginAttemptService.handleLoginFailure(username, userDetailsService::updateAccountNotLockedDB);

        throw exception;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
