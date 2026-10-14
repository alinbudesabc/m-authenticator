package com.alinbudesabc.authenticator.brutforceattak;

import com.alinbudesabc.authenticator.model.dto.FailedAttemptsCount;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final String TEST_USERNAME = "testUser";

//    @InjectMocks
    private LoginAttemptService loginAttemptService;

//    @Test
    void testAddUserToLoginTest() throws ExecutionException {
        loginAttemptService.addUserToLoginAttemptCache(TEST_USERNAME);
        int failedAttemptCount = loginAttemptService.getLoginAttemptCache().get(TEST_USERNAME);
        assertEquals(1, failedAttemptCount);
        assertEquals(0, loginAttemptService.getBlackListUsers().size());
    }

//    @Test
    void testHasExceededMaxAttempts(){
        loginAttemptService.addUserToLoginAttemptCache(TEST_USERNAME);
        loginAttemptService.addUserToLoginAttemptCache(TEST_USERNAME);
        boolean attemptCountHasExceeded= loginAttemptService.hasExceededMaxAttempts(TEST_USERNAME);
        assertTrue(attemptCountHasExceeded);
    }

//    @Test
    void testEvictUserFromLoginAttemptCache() throws ExecutionException {
        loginAttemptService.addUserToLoginAttemptCache(TEST_USERNAME);
        assertEquals(1, loginAttemptService.getLoginAttemptCache().get(TEST_USERNAME));

        loginAttemptService.evictUserFromLoginAttemptCache(TEST_USERNAME);
        assertEquals(0, loginAttemptService.getLoginAttemptCache().get(TEST_USERNAME));
    }

//    @Test
    void testUpdateToMemoryBlacklist() {
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, false);
        FailedAttemptsCount attemptCount = loginAttemptService.getBlackListUsers().get(TEST_USERNAME);

        assertNotNull(attemptCount);
        assertEquals(1, attemptCount.getFailedAttemptsCount());
        assertFalse(attemptCount.isBlacklistedInDB());

        // Simulate repeated failure without being blacklisted
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, false);
        assertEquals(2, attemptCount.getFailedAttemptsCount());

        // Now simulate blacklisting
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, true);
        assertEquals(0, attemptCount.getFailedAttemptsCount());
        assertTrue(attemptCount.isBlacklistedInDB());
    }


//    @Test
    void testIsEligibleForPermanentlyBlock() {
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, false);
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, false); // count = 2

        assertTrue(loginAttemptService.isEligibleForPermanentlyBlock(TEST_USERNAME));
    }

//    @Test
    void testIsAccountPermanentlyBlocked() {
        loginAttemptService.updateToMemoryBlacklist(TEST_USERNAME, true);

        assertTrue(loginAttemptService.isAccountPermanentlyBlocked(TEST_USERNAME));
    }




}