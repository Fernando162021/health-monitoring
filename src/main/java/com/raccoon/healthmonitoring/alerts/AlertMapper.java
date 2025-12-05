package com.raccoon.healthmonitoring.alerts;

import com.raccoon.healthmonitoring.alerts.dto.AlertResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlertMapper {
    AlertResponseDto toAlertResponseDto(Alert alert);
}

