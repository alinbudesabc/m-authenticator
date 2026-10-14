# Two-Factor Authentication (TFA) Flow Documentation

## Overview

This document explains how the `tfa.html` page is used in the two-step authentication flow of the appointment authentication service.

## Complete 2FA Authentication Flow

### Step 1: User Logs In (`/login` endpoint)

**Frontend (login2.html):**
```javascript
// User enters username/password and submits
POST /login
Body: { username: "john", password: "secret123" }
```

**Backend (`MSAuthenticationService.authenticateRESTService`):**
1. Validates username/password
2. Checks if user has 2FA enabled
3. **If 2FA is enabled:**
   - Generates access token (but doesn't send it yet)
   - Stores token in memory: `generatedTokens.put(username, accessToken)` (line 61)
   - Returns: `{ status: 200, jwtToken: null, refreshToken: null, tfaEnabled: true, message: "Two step auth detected" }`
4. **If 2FA is NOT enabled:**
   - Returns tokens immediately: `{ status: 200, jwtToken: "...", refreshToken: "...", tfaEnabled: false }`

### Step 2: Redirect to TFA Page

**Frontend (login2.html lines 184-190):**
```javascript
if(response.tfaEnabled){
    alert("Two-Factor Authentication is required. Redirecting...");
    // Redirects to tfa.html with username in URL
    window.location.href = "/tfa.html?username=" + encodeURIComponent(username);
}
```

**URL becomes:** `/tfa.html?username=john`

### Step 3: User Enters TFA Code (`tfa.html`)

**What happens on tfa.html:**

1. **Page loads and extracts username from URL:**
   ```javascript
   const username = getQueryParam("username"); // Gets "john" from URL
   ```

2. **User enters 6-digit TOTP code** from authenticator app

3. **User clicks "Verify" button** → calls `verifyTFA()` function

4. **`verifyTFA()` sends request:**
   ```javascript
   POST /2fa/validate
   Headers: { Authorization: "jwtToken from localStorage" }
   Body: { username: "john", tfaCode: "123456" }
   ```

### Step 4: Backend Validates TFA Code

**Backend (`RestLoginController.validate2FAToken`):**

1. Calls `authenticationService.validate2FATCode()`:
   - Checks if token exists in `generatedTokens` map (from Step 1)
   - Validates TOTP code against user's secret
   - Returns `Pair<Boolean, String>`

2. **If valid:**
   - Retrieves cached access token: `authenticationService.getTokenForUser(username)`
   - Removes token from cache (one-time use)
   - Returns: `{ status: 200, jwtToken: "actual_token_here", ... }`

3. **If invalid:**
   - Returns: `{ status: 401, jwtToken: null, ... }`

### Step 5: Success → Redirect to Dashboard

**Frontend (tfa.html lines 37-41):**
```javascript
success: function (response) {
    alert("Two-Factor Authentication successful!");
    localStorage.setItem("jwtToken", response.jwtToken) // Store token
    window.location.href = "/dashboard.html"; // Redirect to dashboard
}
```

## Visual Flow Diagram

```
┌─────────────────┐
│  User enters    │
│  username/pwd   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────────┐
│ POST /login     │─────▶│ Backend checks   │
│                 │      │ if 2FA enabled   │
└────────┬────────┘      └────────┬─────────┘
         │                        │
         │ YES                    │
         ▼                        │
┌─────────────────┐              │
│ Response:       │              │
│ tfaEnabled=true │              │
│ jwtToken=null   │              │
└────────┬────────┘              │
         │                        │
         │                        │
         ▼                        │
┌─────────────────┐              │
│ Redirect to     │              │
│ /tfa.html?     │              │
│ username=john   │              │
└────────┬────────┘              │
         │                        │
         │                        │
         ▼                        │
┌─────────────────┐              │
│ User enters     │              │
│ TOTP code       │              │
│ (123456)        │              │
└────────┬────────┘              │
         │                        │
         ▼                        │
┌─────────────────┐              │
│ POST /2fa/      │──────────────┘
│ validate        │
│ {username, code}│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Backend:        │
│ 1. Validates    │
│    TOTP code    │
│ 2. Retrieves    │
│    cached token │
│ 3. Returns token│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Store token &   │
│ redirect to     │
│ dashboard.html  │
└─────────────────┘
```

## Important Points

### 1. Token Caching
The access token is generated during **Step 1** but stored in memory (`generatedTokens` map) until **Step 4**, when it's retrieved and sent to the client.

### 2. Security
The token is **not sent** until 2FA is validated, preventing unauthorized access even if credentials are compromised.

### 3. One-Time Use
The cached token is removed after retrieval (`generatedTokens.remove(username)`), so it can't be reused.

### 4. URL Parameter
The username is passed via query string to `tfa.html` because the token isn't available yet.

### 5. Current Implementation Note
In `RestLoginController` line 55, the response DTO construction may need adjustment to properly return `RESTLoginResponseDto` with all required fields, including `refreshToken` if you want to support refresh tokens for 2FA users.

## Code References

### Frontend Files
- `src/main/resources/static/login2.html` - Login page that redirects to TFA
- `src/main/resources/static/tfa.html` - TFA verification page

### Backend Files
- `src/main/java/com/alinbudesabc/authenticator/controller/RestLoginController.java` - Handles `/login` and `/2fa/validate` endpoints
- `src/main/java/com/alinbudesabc/authenticator/auth/MSAuthenticationService.java` - Core authentication logic

### Key Methods
- `MSAuthenticationService.authenticateRESTService()` - Initial login, caches token if 2FA enabled
- `MSAuthenticationService.validate2FATCode()` - Validates TOTP code
- `MSAuthenticationService.getTokenForUser()` - Retrieves and removes cached token
- `RestLoginController.validate2FAToken()` - Endpoint handler for TFA validation

## Flow Summary

1. **Login** → User submits credentials
2. **Backend Check** → If 2FA enabled, token is cached (not sent)
3. **Redirect** → Frontend redirects to `/tfa.html?username=john`
4. **TFA Input** → User enters TOTP code from authenticator app
5. **Validation** → Backend validates code and retrieves cached token
6. **Success** → Token is sent to client and stored in localStorage
7. **Dashboard** → User is redirected to dashboard with valid token

This two-step process ensures that even if a user's password is compromised, an attacker would still need access to their authenticator app to complete the login process.

