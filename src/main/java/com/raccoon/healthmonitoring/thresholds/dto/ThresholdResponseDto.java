package com.raccoon.healthmonitoring.thresholds.dto;

public record ThresholdResponseDto(
        Long id,
        String metric,
        Double minValue,
        Double maxValue,
        String description
) {}

