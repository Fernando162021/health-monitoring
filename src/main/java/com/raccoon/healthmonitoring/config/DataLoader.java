package com.raccoon.healthmonitoring.config;

import com.raccoon.healthmonitoring.common.enums.Metric;
import com.raccoon.healthmonitoring.thresholds.ThresholdService;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ThresholdService thresholdService;

    @Override
    public void run(String... args) {
        if (thresholdService.getAllThresholds().isEmpty()) {
            log.info("Initializing default thresholds...");

            ThresholdRequestDto heartRateThreshold = new ThresholdRequestDto(
                    Metric.HEART_RATE.name(),
                    60.0,
                    100.0,
                    "Heart rate (beats per minute)"
            );
            thresholdService.createThreshold(heartRateThreshold);

            ThresholdRequestDto oxygenLevelThreshold = new ThresholdRequestDto(
                    Metric.OXYGEN_LEVEL.name(),
                    90.0,
                    null,
                    "Blood oxygen saturation (%)"
            );
            thresholdService.createThreshold(oxygenLevelThreshold);

            ThresholdRequestDto bodyTemperatureThreshold = new ThresholdRequestDto(
                    Metric.BODY_TEMPERATURE.name(),
                    35.0,
                    38.0,
                    "Body temperature (Celsius)"
            );
            thresholdService.createThreshold(bodyTemperatureThreshold);

        } else {
            log.info("Thresholds already initialized (count: {})", thresholdService.getAllThresholds().size());
        }
    }
}
