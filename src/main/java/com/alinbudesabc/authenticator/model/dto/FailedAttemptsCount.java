package com.alinbudesabc.authenticator.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailedAttemptsCount {
    private int failedAttemptsCount;
    private boolean isBlacklistedInDB;
}
