package com.raccoon.healthmonitoring.vitals.dto;

public record VitalStats(
        double avgHeartRate,
        double minOxygenLevel,
        double maxHeartRate,
        double minHeartRate,
        double avgBodyTemperature
) {
}
