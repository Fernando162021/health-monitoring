package com.raccoon.healthmonitoring.thresholds;

import com.raccoon.healthmonitoring.common.enums.Metric;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdRequestDto;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdResponseDto;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdUpdateDto;
import com.raccoon.healthmonitoring.vitals.Vital;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;
    private final ThresholdMapper thresholdMapper;

    public List<ThresholdResponseDto> getAllThresholds() {
        return thresholdRepository.findAll().stream()
                .map(thresholdMapper::toThresholdResponseDto)
                .collect(Collectors.toList());
    }

    public void createThreshold(
            ThresholdRequestDto thresholdRequestDto
    ) {
        if (thresholdRepository.findByMetric(thresholdRequestDto.metric()).isPresent()) {
            throw new IllegalArgumentException("Threshold already exists for metric: " + thresholdRequestDto.metric());
        }

        Threshold threshold = thresholdMapper.toThreshold(thresholdRequestDto);
        thresholdRepository.save(threshold);
    }

    public ThresholdResponseDto updateThresholdValues(ThresholdUpdateDto dto) {
        Threshold threshold = thresholdRepository.findByMetric(dto.metric())
                .orElseThrow(() -> new IllegalArgumentException("Threshold not found for metric: " + dto.metric()));

        threshold.setMinValue(dto.minValue());
        threshold.setMaxValue(dto.maxValue());

        Threshold saved = thresholdRepository.save(threshold);

        return thresholdMapper.toThresholdResponseDto(saved);
    }

    public boolean isVitalOutOfRange(Vital vital) {
        return check(Metric.HEART_RATE, vital.getHeartRate()) ||
               check(Metric.OXYGEN_LEVEL, vital.getOxygenLevel()) ||
               check(Metric.BODY_TEMPERATURE, vital.getBodyTemperature());
    }

    private boolean check(Metric metric, Double value) {
        Threshold threshold = thresholdRepository.findByMetric(metric.name()).orElse(null);
        if (threshold == null || value == null) {
            return false;
        }

        return (threshold.getMinValue() != null && value < threshold.getMinValue())
            || (threshold.getMaxValue() != null && value > threshold.getMaxValue());
    }
}
