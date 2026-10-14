package com.alinbudesabc.authenticator.controller;

import com.alinbudesabc.authenticator.auth.TokenGenerator;
import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenGenerator tokenGenerator;
    private final CustomUserDetailsService userDetailsService;

//    @PostMapping("/refresh-token")
//    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> body) {
//        String refreshToken = body.get("refreshToken");
//        String jwtSecretProp = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9";
//        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretProp));
//
//        try {
//            Claims claims = Jwts.parser().
//                    verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(refreshToken)
//                    .getPayload();
//
//            String username = claims.get(AppConstants.USERNAME, String.class);
//            // Optionally: check refresh token is not blacklisted or expired
//            UserDetails user = userDetailsService.loadUserByUsername(username);
//            String newAccessToken = tokenGenerator.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()), defaultHeaders());
//
//            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
//
//        } catch (JwtException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//    }

}
