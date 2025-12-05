package com.raccoon.healthmonitoring.vitals.dto;

import java.time.LocalDateTime;

public record VitalResponseDto(
        String deviceId,
        Double heartRate,
        Double oxygenLevel,
        Double bodyTemperature,
        Integer steps,
        LocalDateTime createdAt
) {}

