package com.alinbudesabc.authenticator.controller;


import com.alinbudesabc.authenticator.auth.RefreshTokenService;
import com.alinbudesabc.authenticator.auth.util.RequestHeaderExtractor;
import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    private final RequestHeaderExtractor requestHeaderExtractor;

    @PostMapping("refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest httpRequest){
        String refreshToken = httpRequest.getHeader("Authorization");
        if (refreshToken == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }
        RequestHeadersForJWT headers = requestHeaderExtractor.extract(httpRequest);
        String accessToken = refreshTokenService.refreshAccessToken(refreshToken, headers);
        System.out.println("New access token issued by refresh token");
        return ResponseEntity.ok(accessToken);
    }
}
