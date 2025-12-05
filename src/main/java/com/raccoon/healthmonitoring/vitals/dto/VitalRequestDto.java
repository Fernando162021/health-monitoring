package com.raccoon.healthmonitoring.vitals.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for receiving vital data from devices (Node-RED simulation)
 * Example:
 * {
 *   "deviceId": "P-102",
 *   "heartRate": 78,
 *   "oxygenLevel": 96,
 *   "bodyTemperature": 36.7,
 *   "steps": 5400,
 *   "timestamp": "2025-11-10T10:20:00Z"
 * }
 */
public record VitalRequestDto(
        @NotBlank(message = "Device ID is required")
        @Size(max = 50, message = "Device ID cannot exceed 50 characters")
        String deviceId,

        @NotNull(message = "Heart rate is required")
        Double heartRate,

        @NotNull(message = "Oxygen level is required")
        @DecimalMin(value = "0.0", message = "Oxygen level must be at least 0%")
        @DecimalMax(value = "100.0", message = "Oxygen level cannot exceed 100%")
        Double oxygenLevel,

        @NotNull(message = "Body temperature is required")
        Double bodyTemperature,

        @NotNull(message = "Steps count is required")
        @Min(value = 0, message = "Steps cannot be negative")
        Integer steps
) {}

