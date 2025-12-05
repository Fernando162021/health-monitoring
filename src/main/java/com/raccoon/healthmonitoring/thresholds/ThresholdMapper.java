package com.raccoon.healthmonitoring.thresholds;

import com.raccoon.healthmonitoring.thresholds.dto.ThresholdRequestDto;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThresholdMapper {
    ThresholdResponseDto toThresholdResponseDto(Threshold threshold);
    Threshold toThreshold(ThresholdRequestDto thresholdRequestDto);
}