package com.alinbudesabc.authenticator.model.dto;

import lombok.Builder;

@Builder
public record RequestHeadersForJWT(
        String ipAddress,
        String userAgent,
        String screenRes,
        String timezone
) {
}
