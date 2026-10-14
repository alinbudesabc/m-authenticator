package com.alinbudesabc.authenticator.auth.util;

import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestHeaderExtractor {

    private static final List<String> TRUSTED_PROXIES = List.of("127.0.0.1", "::1");

    public RequestHeadersForJWT extract(HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String screenRes = request.getHeader("X-Screen-Resolution");
        String timezone = request.getHeader("X-Timezone");

        return RequestHeadersForJWT.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .screenRes(screenRes)
                .timezone(timezone)
                .build();
    }


    // TODO:  we need to configure proxies if we have. ex, nginx, aws, load balancer etc
    // TODO: they should add the real ip from TCP protocol
    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr(); // this should be the proxy ip if they are present
        String clientIp = null;
        if (TRUSTED_PROXIES.contains(remoteAddr)) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isEmpty()) {
                clientIp = xff.split(",")[0].trim(); // Get the first IP in the chain. Proxy always add the real ip as first element
            }
        }
        if (clientIp == null) {
            clientIp = remoteAddr; // Fallback: there are no proxies, use direct IP.
        }
        return clientIp;
    }
}