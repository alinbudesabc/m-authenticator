package com.alinbudesabc.authenticator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizationEventsListener {

    @EventListener
    public void onFailure(AuthorizationDeniedEvent authorizationDeniedEvent){
        log.error("Authorization failed for the user: {} due to {} ", authorizationDeniedEvent.getAuthentication().get().getName(),
        authorizationDeniedEvent.getAuthorizationDecision());
        log.error("Authorities: " );
        for (GrantedAuthority role : authorizationDeniedEvent.getAuthentication().get().getAuthorities()){
            log.info("{} ", role);
        }
    }


}
