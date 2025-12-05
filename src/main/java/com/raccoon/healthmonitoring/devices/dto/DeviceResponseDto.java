package com.raccoon.healthmonitoring.devices.dto;

import com.raccoon.healthmonitoring.vitals.dto.VitalResponseDto;

public record DeviceResponseDto(
        String deviceId,
        Boolean isActive,
        VitalResponseDto latestVital
) {
}
