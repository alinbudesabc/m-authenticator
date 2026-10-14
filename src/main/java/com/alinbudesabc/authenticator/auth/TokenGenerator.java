package com.alinbudesabc.authenticator.auth;

import com.alinbudesabc.authenticator.constants.AppConstants;
import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

import static com.alinbudesabc.authenticator.constants.AppConstants.APP_NAME;
import static com.alinbudesabc.authenticator.constants.AppConstants.JWT_TOKEN;

@Component
public class TokenGenerator {

    @Value("${jwt.secret}")
    private String jwtSecretProp;

    @Value("${jwt.expiration.time}")
    private Long jwtExpirationTime;

       @Value("${jwt.refresh.secret}")
    private String refreshTokenSecret;

    @Value("${jwt.refresh.expiration.time}")
    private Long refreshTokenExpirationTime;

    public String generateAccessToken(Authentication authentication, RequestHeadersForJWT headers){
        return generateToken(authentication, headers, false);
    }

    public String generateRefreshToken(Authentication authentication, RequestHeadersForJWT headers){
        return generateToken(authentication, headers, true);
    }

    public String generateToken(Authentication authentication, RequestHeadersForJWT headers, boolean isRefreshToken){
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretProp));
        long expirationTime = isRefreshToken ? refreshTokenExpirationTime : jwtExpirationTime;
        Date now = new Date();

        var builder = Jwts.builder()
                .issuer(APP_NAME)
                .subject(JWT_TOKEN)
                .claim(AppConstants.USERNAME, authentication.getName())
                .claim(AppConstants.IP_ADDRESS, headers.ipAddress())
                .claim(AppConstants.USER_AGENT, headers.userAgent())
                .claim(AppConstants.TIMEZONE, headers.timezone())
                .claim(AppConstants.SCREEN_RES, headers.screenRes())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime));
         
        if (!isRefreshToken) {
            builder.claim(AppConstants.AUTHORITIES, authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
        }        
                
        return builder.signWith(secretKey).compact();
    }
}
