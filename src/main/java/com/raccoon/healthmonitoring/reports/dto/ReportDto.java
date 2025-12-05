package com.raccoon.healthmonitoring.reports.dto;

public record ReportDto(
        String deviceId,
        Integer timeWindow,          // 5, 15, or 60 minutes
        Double avgHeartRate,
        Double minOxygenLevel,
        Double maxHeartRate,
        Double minHeartRate,
        Double avgBodyTemperature,
        Long alertCount,
        Long totalReadings
) {}