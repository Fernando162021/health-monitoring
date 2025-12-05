package com.raccoon.healthmonitoring.alerts.dto;

import java.time.LocalDateTime;

public record AlertResponseDto(
        Long id,
        String deviceId,
        String metric,
        Double value,
        String threshold,
        LocalDateTime triggeredAt,
        Boolean acknowledged,
        LocalDateTime acknowledgedAt
) {}





