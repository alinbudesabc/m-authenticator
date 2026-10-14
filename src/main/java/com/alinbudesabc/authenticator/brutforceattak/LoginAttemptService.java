package com.alinbudesabc.authenticator.brutforceattak;

import com.alinbudesabc.authenticator.exception.BlockedUserException;
import com.alinbudesabc.authenticator.exception.TemporallyBlockedAccountException;
import com.alinbudesabc.authenticator.model.dto.FailedAttemptsCount;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@Service
@Getter
public class LoginAttemptService {
    private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 2;
    public static final int SUSPICIOUS_ATTACK_VALUE = 2;

//    @Value("${app.login.cache.expire}")
//    private Integer loginCacheExpire;

    private final LoadingCache<String, Integer>   loginAttemptCache;
    private final Map<String, FailedAttemptsCount> blackListUsers = new HashMap<>();

    public LoginAttemptService(@Value("${app.login.cache.expire}") Integer loginCacheExpire){
        loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(loginCacheExpire, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<String, Integer>() {
                        @Override
                        public Integer load(String key) throws Exception {
                            return 0;
                        }
                });
    }

    public void evictUserFromLoginAttemptCache(String username){
        loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username){
        int attempts = 0;
        try{
            attempts = loginAttemptCache.get(username) + 1;
            loginAttemptCache.put(username, attempts);
        }catch(ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean hasExceededMaxAttempts(String username) {
        try {
            return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e) {
            log.error("Object couldn't be extracted from cache: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //If the user has already been temporarily blocked, they will be kept in the blacklist cache
    // for further usage(usage ex: if threshold is exceeded then the user from this list will be blocked in the db)
    public void updateToMemoryBlacklist(String username, boolean blacklisted) {
        blackListUsers.compute(username, (key, failedAttemptCount) -> {
            if (failedAttemptCount == null) {
                return new FailedAttemptsCount(1, false);
            }
            if(blacklisted){
                failedAttemptCount.setFailedAttemptsCount(0); //reset count
            } else {
                failedAttemptCount.setFailedAttemptsCount(failedAttemptCount.getFailedAttemptsCount() + 1);
            }
            failedAttemptCount.setBlacklistedInDB(blacklisted);
            return failedAttemptCount;
        });
        log.debug("Updated user '{}' in cache. Blacklisted: {}", username, blacklisted);
    }

    public boolean isEligibleForPermanentlyBlock(String username){

        return Optional.ofNullable(blackListUsers.get(username))
                .map(count -> count.getFailedAttemptsCount() >= SUSPICIOUS_ATTACK_VALUE && !count.isBlacklistedInDB())
                .orElse(false);
    }

    public boolean isAccountPermanentlyBlocked(String username) {
        return Optional.ofNullable(blackListUsers.get(username))
                .map(FailedAttemptsCount::isBlacklistedInDB)
                .orElse(false);
    }


    public void handleLoginFailure(String username, BiConsumer<String, Boolean> dbBlockConsumer) {
        // Clean stale blacklist state if temp lock expired
        if (!loginAttemptCache.asMap().containsKey(username)) {
            FailedAttemptsCount count = blackListUsers.get(username);
            if (count != null && !count.isBlacklistedInDB()) {
                log.debug("Resetting blacklist cache for user '{}' due to expired loginAttemptCache", username);
                blackListUsers.remove(username);
            }
        }
        try {
            // 1. Increment short-term cache
            int attempts = loginAttemptCache.get(username) + 1;
            loginAttemptCache.put(username, attempts);

            // 2. Check temp block threshold
            if (attempts >= MAXIMUM_NUMBER_OF_ATTEMPTS) {
                // 3. Promote to blacklist tracking
                FailedAttemptsCount current = blackListUsers.getOrDefault(username, new FailedAttemptsCount(0, false));
                int newCount = current.getFailedAttemptsCount() + 1;

                FailedAttemptsCount updated = new FailedAttemptsCount(newCount, current.isBlacklistedInDB());
                blackListUsers.put(username, updated);

                // 4. Check if eligible for permanent block
                if (isEligibleForPermanentlyBlock(username)) {
                    dbBlockConsumer.accept(username, false); //update in db
                    updateToMemoryBlacklist(username, true);//update in synchronized list
                    throw new BlockedUserException("Account is permanently locked.");
                }

                // 5. Otherwise, temporary lock
                updateToMemoryBlacklist(username, false);
                throw new TemporallyBlockedAccountException("Account temporarily suspended due to too many wrong credentials");
            } else {
                throw new BadCredentialsException("Bad credentials");
            }

        } catch (ExecutionException e) {
            log.error("Error accessing loginAttemptCache: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void handleLoginSuccess(String username) {
        evictUserFromLoginAttemptCache(username);  // Remove temp tracking
        blackListUsers.remove(username);           // Clear blacklist tracking if user succeeded
    }

    public void enablePermanentlyBlockedAccount(String username, Consumer<Boolean> dbEnabler){
        this.blackListUsers.remove(username);
        evictUserFromLoginAttemptCache(username);
        dbEnabler.accept(true);
    }
}
