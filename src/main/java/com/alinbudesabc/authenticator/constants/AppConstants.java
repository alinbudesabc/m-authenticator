package com.alinbudesabc.authenticator.constants;

import java.util.Set;

public final class AppConstants {
    public static final String JWT_SECRET = "jwt-secret";
    public static final String APP_NAME = "Authenticator App";
    //arbitrary value for token creation
    public static final String JWT_TOKEN = "JWT_TOKEN";
    public static final String JWT_AUTH_HEADER = "Authorization";
    public static final String JWT_REFRESH_AUTH_HEADER = "RefreshToken";
    public static final byte RESET_PASS_EXPIRATION_TIME = 8;
    public static final String RESET_PASS_EMAIL_SENT = "An email with the reset link was sent";
    public static final String USERNAME = "username";
    public static final String AUTHORITIES = "authorities";

    //JWT-CLAIMS
    public static final String USER_AGENT = "userAgent";
    public static final String TIMEZONE = "timezone";
    public static final String SCREEN_RES = "screeRes";
    public static final String IP_ADDRESS = "ip";

    public static final Set<String> PUBLIC_PATHS = Set.of(
            "/user", "/login", "/resetPassword", "/verifyEmail",
            "/2fa/validate", "/style/style.css", "/favicon.ico",
            "/2fa/qrcode/get", "/basicAuth", "/profile", "/refresh-token"
    );
}
