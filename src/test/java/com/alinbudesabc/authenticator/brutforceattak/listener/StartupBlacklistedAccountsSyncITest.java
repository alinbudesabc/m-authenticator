package com.alinbudesabc.authenticator.brutforceattak.listener;

import com.alinbudesabc.authenticator.BaseITest;
import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.FailedAttemptsCount;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ContextConfiguration(initializers = {BaseITest.Initializer.class})
public class StartupBlacklistedAccountsSyncITest extends BaseITest{


    @Autowired
    private  LoginAttemptService loginAttemptService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StartupBlacklistedAccountsSync blacklistedAccountsSync;

    @BeforeEach
    void setup(){
        UserEntity user = userRepository.save(buildUser());
        log.info("user saved in db: {}", user);
    }

    @Test
    @DisplayName("Test if user is successfully registered")
    public void checkIfBlacklistedUsersAreLoadedFromDbAtStartup() {
        String username = "testuser";

        blacklistedAccountsSync.synchronizeBlacklistedAccounts();

        Map<String, FailedAttemptsCount> cachedBlacklist = loginAttemptService.getBlackListUsers();

        assertTrue(cachedBlacklist.containsKey(username), "Blacklisted user should be in the cache");
        FailedAttemptsCount count = cachedBlacklist.get(username);
        assertNotNull(count, "FailedAttemptsCount should not be null");
        assertTrue(count.isBlacklistedInDB(), "User should be marked as blacklisted");
    }


    private UserEntity buildUser() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("password123");

        return UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("abc@gmail.com")
                .password(encodedPassword)
                .firstName("John")
                .lastName("Doe")
                .isAccountNonExpired(true)
                .isAccountNonLocked(false)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .twoFactorEnabled(false)
                .authStrategy(AuthTypeEnum.ONE_STEP_AUTH)
                .twoFactorMessageStrategy(MessagingStrategyEnum.NONE)
                .mfaSecret("secret")
                .build();
    }
}