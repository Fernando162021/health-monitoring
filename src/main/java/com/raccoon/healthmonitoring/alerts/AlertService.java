package com.raccoon.healthmonitoring.alerts;

import com.raccoon.healthmonitoring.alerts.dto.AlertResponseDto;
import com.raccoon.healthmonitoring.common.enums.Metric;
import com.raccoon.healthmonitoring.devices.Device;
import com.raccoon.healthmonitoring.graphql.DeviceGraphQLHandler;
import com.raccoon.healthmonitoring.thresholds.Threshold;
import com.raccoon.healthmonitoring.thresholds.ThresholdRepository;
import com.raccoon.healthmonitoring.vitals.Vital;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final ThresholdRepository thresholdRepository;
    private final AlertMapper alertMapper;

    public List<AlertResponseDto> getActiveAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByTriggeredAtDesc()
                .stream()
                .map(alertMapper::toAlertResponseDto)
                .collect(Collectors.toList());
    }

    public List<AlertResponseDto> getAlertsByDevice(String deviceId) {
        return alertRepository.findByDeviceIdOrderByTriggeredAtDesc(deviceId)
                .stream()
                .map(alertMapper::toAlertResponseDto)
                .collect(Collectors.toList());
    }

    public Long countAlertsByDeviceAndTimeRange(String deviceId, LocalDateTime since) {
        return alertRepository.findByDeviceIdOrderByTriggeredAtDesc(deviceId)
                .stream()
                .filter(alert -> !alert.getTriggeredAt().isBefore(since))
                .count();
    }

    public List<AlertResponseDto> getAlertHistory(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertRepository.findRecentAlerts(since)
                .stream()
                .map(alertMapper::toAlertResponseDto)
                .collect(Collectors.toList());
    }

    public List<AlertResponseDto> acknowledgeAllAlertsForDevice(String deviceId) {
        List<Alert> alerts = alertRepository.findByDeviceIdAndAcknowledgedFalseOrderByTriggeredAtDesc(deviceId);

        LocalDateTime now = LocalDateTime.now();
        for (Alert alert : alerts) {
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(now);
        }

        List<Alert> savedAlerts = alertRepository.saveAll(alerts);
        log.info("Acknowledged {} alerts for device {}", savedAlerts.size(), deviceId);

        return savedAlerts.stream()
                .map(alertMapper::toAlertResponseDto)
                .collect(Collectors.toList());
    }

    public void createAlertsFromVital(Vital vital, Device device) {
        checkAndCreateAlert(vital, device, Metric.HEART_RATE, vital.getHeartRate());
        checkAndCreateAlert(vital, device, Metric.OXYGEN_LEVEL, vital.getOxygenLevel());
        checkAndCreateAlert(vital, device, Metric.BODY_TEMPERATURE, vital.getBodyTemperature());
    }

    // helper to check thresholds and create alert if needed
    private void checkAndCreateAlert(Vital vital, Device device, Metric metric, Double value) {
        if (value == null) {
            return;
        }

        Threshold threshold = thresholdRepository.findByMetric(metric.name())
                .orElseThrow(() -> new IllegalArgumentException("Threshold not found for metric: " + metric.name()));

        if (threshold.getMaxValue() != null && value > threshold.getMaxValue()) {
            createAlert(vital, device, metric.name(), value, "ABOVE " + threshold.getMaxValue());
        }

        else if (threshold.getMinValue() != null && value < threshold.getMinValue()) {
            createAlert(vital, device, metric.name(), value, "BELOW " + threshold.getMinValue());
        }
    }

    // helper to create and save alert
    private void createAlert(Vital vital, Device device, String metric, Double value, String threshold) {
        Alert alert = Alert.builder()
                .deviceId(vital.getDeviceId())
                .device(device)
                .vital(vital)
                .metric(metric)
                .value(value)
                .threshold(threshold)
                .triggeredAt(vital.getCreatedAt())
                .acknowledged(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert created: {} {} {} for device {}", metric, threshold, value, vital.getDeviceId());

        DeviceGraphQLHandler.publishAlert(savedAlert);
    }
}
