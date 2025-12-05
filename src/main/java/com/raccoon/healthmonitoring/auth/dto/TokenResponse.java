package com.raccoon.healthmonitoring.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
