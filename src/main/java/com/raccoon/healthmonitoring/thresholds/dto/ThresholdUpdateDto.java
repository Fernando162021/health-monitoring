package com.raccoon.healthmonitoring.thresholds.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating threshold values
 */
public record ThresholdUpdateDto(
        @NotBlank(message = "Metric is required")
        String metric,
        Double minValue,
        Double maxValue
) {}

