package com.raccoon.healthmonitoring.vitals;

import com.raccoon.healthmonitoring.vitals.dto.VitalResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VitalMapper {
    VitalResponseDto toVitalResponseDto(Vital vital);
}

