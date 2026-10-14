package com.alinbudesabc.authenticator.brutforceattak.listener;


import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.model.dto.FailedAttemptsCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupBlacklistedAccountsSync {

    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;

    @EventListener(ApplicationReadyEvent.class)
    public void synchronizeBlacklistedAccounts(){
        Map<String, FailedAttemptsCount> cachedUsers =
                loginAttemptService.getBlackListUsers();

        userDetailsService.getBalcklistedUsers().forEach(username -> {
            {
                cachedUsers.put(username, new FailedAttemptsCount(0, true));
            }
        });
        log.info("Total blacklisted users loaded into cache: {}", cachedUsers.size());
        cachedUsers.forEach((username, details) ->
                log.debug("User: {}, Details: {}", username, details)
        );
    }

}
