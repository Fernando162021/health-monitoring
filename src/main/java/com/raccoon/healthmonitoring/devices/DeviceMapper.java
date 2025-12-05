package com.raccoon.healthmonitoring.devices;

import com.raccoon.healthmonitoring.devices.dto.DeviceResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    DeviceResponseDto toDeviceResponseDto(Device device);
}
