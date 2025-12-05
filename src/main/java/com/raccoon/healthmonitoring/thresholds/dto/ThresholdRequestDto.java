package com.raccoon.healthmonitoring.thresholds.dto;

import jakarta.persistence.Column;

public record ThresholdRequestDto(
        String metric, // "heartRate", "oxygenLevel", "bodyTemperature"
        Double minValue,
        Double maxValue,
        String description
) {
}