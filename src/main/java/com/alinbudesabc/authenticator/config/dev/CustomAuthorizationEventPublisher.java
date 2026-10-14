package com.alinbudesabc.authenticator.config.dev;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.event.AuthorizationGrantedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
@Component
public class CustomAuthorizationEventPublisher implements AuthorizationEventPublisher {

    private final ApplicationEventPublisher publisher;
    private final AuthorizationEventPublisher delegate;

    public CustomAuthorizationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.delegate = new SpringAuthorizationEventPublisher(publisher);
    }

    @EventListener
    public void onFailure(AuthorizationDeniedEvent authorizationDeniedEvent){
        log.error("Authorization failed for the user: {} due to {} ", authorizationDeniedEvent.getAuthentication().get().getName(),
                authorizationDeniedEvent.getAuthorizationDecision());
    }
    @Override
    public <T> void publishAuthorizationEvent(Supplier<Authentication> authentication, T object, AuthorizationDecision decision) {
        if (decision == null) {
            return;
        }
        if (!decision.isGranted()) {
            this.delegate.publishAuthorizationEvent(authentication, object, decision);
            return;
        }
        if (shouldThisEventBePublished(decision)) {
            AuthorizationGrantedEvent granted = new AuthorizationGrantedEvent(
                    authentication, object, decision);
            this.publisher.publishEvent(granted);
        }
    }

    private boolean shouldThisEventBePublished(AuthorizationDecision decision) {
        if (!(decision instanceof AuthorityAuthorizationDecision)) {
            return false;
        }
        Collection<GrantedAuthority> authorities = ((AuthorityAuthorizationDecision) decision).getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if ("ROLE_USER".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
