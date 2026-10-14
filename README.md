# authentication-service
Authentication service which supports multiple authentication options

## Brute-force Protection Logic
## 📌 Overview

This application implements custom authentication logic using Spring Security. It enhances security by introducing layered protection against brute-force login attempts through:

- Short-term login attempt tracking (with expiry).
- Long-term suspicious behavior detection.
- Automatic temporary and permanent user blocking.

### 1. Temporary Blocking (Short-term cache)

- Managed via **Guava's `LoadingCache`** (`loginAttemptCache`)
- Cache auto-expires after **x minutes**.
- Each failed login attempt increments the counter.
- Exceeding `MAXIMUM_NUMBER_OF_ATTEMPTS` results in a **temporary block**.

### 2. Suspicious Tracking (Persistent Map)

- Managed via `Map<String, FailedAttemptsCount>` (`blackListUsers`)
- If a user repeatedly exceeds temporary thresholds, they're tracked as suspicious.
- After enough repeated failed blocks (`SUSPICIOUS_ATTACK_VALUE`), they're marked for **permanent blocking**.

### 3. Permanent Blocking
- Once a user is permanently blocked, their status is updated in the database and in memory.
- Any further login attempts will be denied immediately.
- Any further authentication attempt results in a `BlockedUserException`.

## Design Decisions
- Used Guava's `LoadingCache` for auto-expiry logic.
- Used a `Map<String, FailedAttemptsCount>` for persistent tracking of suspicious users.
- I use separate lists for temporally baned users and permanent blocked users because I want to avoid checks to db when checking if user is locked. I keep them in cache instead and the list is synchronized with the db at the start of the application
- Introduced `BiConsumer<String, Boolean>` to decouple DB logic from service.


## Future Improvements
- Move blacklist to Redis for distributed systems.
- Add audit logs for suspicious login attempts.

