package com.alinbudesabc.authenticator.filter;

import com.alinbudesabc.authenticator.auth.util.RequestHeaderExtractor;
import com.alinbudesabc.authenticator.constants.AppConstants;
import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Objects;

import static com.alinbudesabc.authenticator.constants.AppConstants.PUBLIC_PATHS;

@Slf4j
@RequiredArgsConstructor
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private final RequestHeaderExtractor headerExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String jwt = request.getHeader(AppConstants.JWT_AUTH_HEADER);
        //TODO: save this key in properties/environment variable
        String jwtSecretProp = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9";
        if(null != jwt){
            RequestHeadersForJWT headers = headerExtractor.extract(request);
            try{
                SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretProp));
                if(null != secretKey){
                    Claims claims = Jwts.parser().
                            verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(jwt)
                            .getPayload();
                    String username = String.valueOf(claims.get(AppConstants.USERNAME));
                    String authoritiesAsString = String.valueOf(claims.get(AppConstants.AUTHORITIES));
                    String tokenIp = String.valueOf(claims.get(AppConstants.IP_ADDRESS));
                    String tokenUa = String.valueOf(claims.get(AppConstants.USER_AGENT));
                    String tokenSr = String.valueOf(claims.get(AppConstants.SCREEN_RES));
                    String tokenTz = String.valueOf(claims.get(AppConstants.TIMEZONE));

                    if (!headers.ipAddress().equals(tokenIp) ||
                            !Objects.equals(headers.userAgent(), tokenUa) ||
                            !Objects.equals(headers.screenRes(), tokenSr) ||
                            !Objects.equals(headers.timezone(), tokenTz)) {

                        log.warn("Client attributes do not match token claims");
                        throw new BadCredentialsException("Token claims do not match");
                    }

                    request.setAttribute("REAL_CLIENT_IP", headers.ipAddress()); //make it available for downstream services

                    Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                            AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesAsString));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                //TODO: send some reports with error actions or dangerous actions to queue and create a service that save them in mongo db and integrate with slack/teams..etc
            }
            catch (ExpiredJwtException e) {
                log.warn("JWT Token has expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired: " + e.getMessage());
                response.getWriter().flush();
                return;
            }
            catch(Exception e){
                throw new BadCredentialsException("Invalid token received!");
            }

        }

        filterChain.doFilter(request, response);
    }

//    // TODO:  we need to configure proxies if we have. ex, nginx, aws, load balancer etc
//    // TODO: they should add the real ip from TCP protocol
//    private String extractClientIp(HttpServletRequest request) {
//        String remoteAddr = request.getRemoteAddr(); // this should be the proxy ip if they are present
//        String clientIp = null;
//
//        if (TRUSTED_PROXIES.contains(remoteAddr)) {
//            String xff = request.getHeader("X-Forwarded-For");
//            if (xff != null && !xff.isEmpty()) {
//                clientIp = xff.split(",")[0].trim(); // Get the first IP in the chain. Proxy always add the real ip as first element
//            }
//        }
//
//        if (clientIp == null) {
//            clientIp = remoteAddr; // Fallback: there are no proxies, use direct IP.
//        }
//        return clientIp;
//    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return PUBLIC_PATHS.contains(request.getServletPath());
    }
}
