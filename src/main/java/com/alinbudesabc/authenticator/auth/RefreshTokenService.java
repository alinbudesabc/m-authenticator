package com.alinbudesabc.authenticator.auth;

import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.constants.AppConstants;
import com.alinbudesabc.authenticator.exception.InvalidRefreshTokenException;
import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
@Component
public class RefreshTokenService {

    private final CustomUserDetailsService userDetailsService;
    private final TokenGenerator tokenGenerator;

    public String refreshAccessToken(String refreshToken, RequestHeadersForJWT requestHeadersForJWT){
        Claims claims = validateRefreshToken(refreshToken, requestHeadersForJWT.ipAddress());
        String username = claims.get(AppConstants.USERNAME, String.class);
        UserDetails user = userDetailsService.loadUserByUsername(username);
        return tokenGenerator.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()), requestHeadersForJWT, true);
    }

    //TODO: create separate validator for token
    public Claims validateRefreshToken(String refreshToken, String ipAddress){
        String jwtSecretProp = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9";
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretProp));
        Claims claims;
        try {
        claims = Jwts.parser().
                verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
        } catch (JwtException e) {
            System.out.println("Invalid or expired token");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        if (!claims.get(AppConstants.IP_ADDRESS).equals(ipAddress)){
            throw new InvalidRefreshTokenException("Invalid address");
        }
        return claims;
    }
}
