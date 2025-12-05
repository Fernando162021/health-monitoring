package com.raccoon.healthmonitoring.reports;

import com.raccoon.healthmonitoring.alerts.AlertService;
import com.raccoon.healthmonitoring.devices.DeviceRepository;
import com.raccoon.healthmonitoring.reports.dto.ReportDto;
import com.raccoon.healthmonitoring.vitals.Vital;
import com.raccoon.healthmonitoring.vitals.VitalRepository;
import com.raccoon.healthmonitoring.vitals.dto.VitalStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final VitalRepository vitalRepository;
    private final DeviceRepository deviceRepository;
    private final AlertService alertService;

    public ReportDto getReport(String deviceId, Integer window) {
        log.debug("Generating report: deviceId={}, window={} minutes", deviceId, window);

        validateDevice(deviceId);

        LocalDateTime since = LocalDateTime.now().minusMinutes(window);
        List<Vital> vitals = getVitalsByDeviceAndTime(deviceId, since);

        if (vitals.isEmpty()) {
            log.warn("No vitals found for device {} in the last {} minutes", deviceId, window);
            return createEmptyReport(deviceId, window);
        }

        VitalStats stats = calculateVitalStats(vitals);
        Long alertCount = alertService.countAlertsByDeviceAndTimeRange(deviceId, since);

        log.info("Report generated for device {}: {} vitals, {} alerts in last {} minutes",
            deviceId, vitals.size(), alertCount, window);

        return new ReportDto(
                deviceId,
                window,
                roundToTwoDecimals(stats.avgHeartRate()),
                roundToTwoDecimals(stats.minOxygenLevel()),
                roundToTwoDecimals(stats.maxHeartRate()),
                roundToTwoDecimals(stats.minHeartRate()),
                roundToTwoDecimals(stats.avgBodyTemperature()),
                alertCount,
                (long) vitals.size()
        );
    }
    
    public String exportReportAsCSV(String deviceId, Integer window) {
        log.info("Exporting CSV report: deviceId={}, window={} minutes", deviceId, window);

        validateDevice(deviceId);

        LocalDateTime since = LocalDateTime.now().minusMinutes(window);
        List<Vital> vitals = getVitalsByDeviceAndTime(deviceId, since);

        log.debug("CSV export contains {} vital records for device {}", vitals.size(), deviceId);
        return buildCSVContent(vitals);
    }

    private void validateDevice(String deviceId) {
        deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
    }

    private List<Vital> getVitalsByDeviceAndTime(String deviceId, LocalDateTime since) {
        return vitalRepository.findRecentVitals(since)
                .stream()
                .filter(v -> v.getDeviceId().equals(deviceId))
                .toList();
    }

    private ReportDto createEmptyReport(String deviceId, Integer window) {
        return new ReportDto(deviceId, window, 0.0, 0.0, 0.0, 0.0, 0.0, 0L, 0L);
    }

    private VitalStats calculateVitalStats(List<Vital> vitals) {
        if (vitals.isEmpty()) {
            return new VitalStats(0.0, 0.0, 0.0, 0.0, 0.0);
        }

        double sumHeartRate = 0.0;
        double sumBodyTemp = 0.0;
        double minHeartRate = Double.MAX_VALUE;
        double maxHeartRate = Double.MIN_VALUE;
        double minOxygenLevel = Double.MAX_VALUE;

        for (Vital vital : vitals) {
            double heartRate = vital.getHeartRate();
            double oxygenLevel = vital.getOxygenLevel();
            double bodyTemp = vital.getBodyTemperature();

            sumHeartRate += heartRate;
            sumBodyTemp += bodyTemp;

            minHeartRate = Math.min(minHeartRate, heartRate);
            maxHeartRate = Math.max(maxHeartRate, heartRate);
            minOxygenLevel = Math.min(minOxygenLevel, oxygenLevel);
        }

        int count = vitals.size();
        return new VitalStats(
                sumHeartRate / count,
                minOxygenLevel,
                maxHeartRate,
                minHeartRate,
                sumBodyTemp / count
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String buildCSVContent(List<Vital> vitals) {
        StringBuilder csv = new StringBuilder();
        csv.append("Device ID,Timestamp,Heart Rate,Oxygen Level,Body Temperature,Steps\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Vital vital : vitals) {
            csv.append(vital.getDeviceId()).append(",")
                    .append(vital.getCreatedAt().format(formatter)).append(",")
                    .append(vital.getHeartRate()).append(",")
                    .append(vital.getOxygenLevel()).append(",")
                    .append(vital.getBodyTemperature()).append(",")
                    .append(vital.getSteps()).append("\n");
        }

        return csv.toString();
    }
}
