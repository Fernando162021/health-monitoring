package com.raccoon.healthmonitoring.devices.dto;

/**
 * DTO for Overview Tab - KPI Cards
 */
public record DeviceStatsDto(
        Long devicesMonitored,      // Total active devices
        Long activeAlerts,          // Devices with vitals out of normal range
        Double avgHeartRateLast5Min // Average heart rate from last 5 minutes
) {}



