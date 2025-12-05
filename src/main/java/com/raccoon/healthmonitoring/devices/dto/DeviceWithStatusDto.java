package com.raccoon.healthmonitoring.devices.dto;

import java.time.LocalDateTime;

/**
 * Note: 'status' is calculated on-the-fly, not stored in DB
 */
public record DeviceWithStatusDto(
        String deviceId,             // Device identifier (e.g., "P-102")
        String status,               // "OK", "ALERT", or "NO_DATA" (calculated)
        Double heartRate,            // HR
        Double oxygenLevel,          // SpO₂
        Double bodyTemperature,
        Integer steps,
        LocalDateTime lastUpdate
) {}

