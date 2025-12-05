package com.raccoon.healthmonitoring.health;

import com.raccoon.healthmonitoring.alerts.AlertRepository;
import com.raccoon.healthmonitoring.devices.DeviceRepository;
import com.raccoon.healthmonitoring.thresholds.ThresholdRepository;
import com.raccoon.healthmonitoring.users.UserRepository;
import com.raccoon.healthmonitoring.vitals.VitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final VitalRepository vitalRepository;
    private final AlertRepository alertRepository;
    private final ThresholdRepository thresholdRepository;

    private final LocalDateTime startTime = LocalDateTime.now();

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check endpoint accessed at {}", LocalDateTime.now());

        Map<String, Object> health = new LinkedHashMap<>();
        boolean allChecksPass = true;

        try {
            // App info
            health.put("application", "Health Monitoring API");
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("uptime", getUptimeString());

            // Db connectivity checks
            Map<String, Object> database = new LinkedHashMap<>();
            try {
                long deviceCount = deviceRepository.count();
                long vitalCount = vitalRepository.count();
                long userCount = userRepository.count();
                long alertCount = alertRepository.count();
                long thresholdCount = thresholdRepository.count();

                database.put("status", "UP");
                database.put("type", "PostgreSQL");
                database.put("responsive", true);

                // Entity counts
                Map<String, Object> counts = new LinkedHashMap<>();
                counts.put("devices", deviceCount);
                counts.put("vitals", vitalCount);
                counts.put("users", userCount);
                counts.put("alerts", alertCount);
                counts.put("thresholds", thresholdCount);
                database.put("entities", counts);

                log.debug("Database health check passed - Devices: {}, Vitals: {}, Users: {}, Alerts: {}, Thresholds: {}",
                    deviceCount, vitalCount, userCount, alertCount, thresholdCount);

            } catch (Exception e) {
                database.put("status", "DOWN");
                database.put("error", e.getMessage());
                database.put("responsive", false);
                allChecksPass = false;
                log.error("Database health check failed", e);
            }
            health.put("database", database);

            // Active devices check
            Map<String, Object> devices = new LinkedHashMap<>();
            try {
                long activeDevices = deviceRepository.findAllByIsActiveTrue().size();
                long inactiveDevices = deviceRepository.count() - activeDevices;

                devices.put("status", "UP");
                devices.put("active", activeDevices);
                devices.put("inactive", inactiveDevices);
                devices.put("total", activeDevices + inactiveDevices);

            } catch (Exception e) {
                devices.put("status", "DOWN");
                devices.put("error", e.getMessage());
                allChecksPass = false;
                log.error("Devices health check failed", e);
            }
            health.put("devices", devices);

            // Alerts monitoring
            Map<String, Object> alerts = new LinkedHashMap<>();
            try {
                long activeAlerts = alertRepository.findByAcknowledgedFalseOrderByTriggeredAtDesc().size();
                long totalAlerts = alertRepository.count();
                long acknowledgedAlerts = totalAlerts - activeAlerts;

                alerts.put("status", activeAlerts > 100 ? "WARNING" : "UP");
                alerts.put("active", activeAlerts);
                alerts.put("acknowledged", acknowledgedAlerts);
                alerts.put("total", totalAlerts);

                if (activeAlerts > 100) {
                    alerts.put("warning", "High number of active alerts detected");
                    log.warn("High number of active alerts: {}", activeAlerts);
                }

            } catch (Exception e) {
                alerts.put("status", "DOWN");
                alerts.put("error", e.getMessage());
                allChecksPass = false;
                log.error("Alerts health check failed", e);
            }
            health.put("alerts", alerts);

            // System resources
            Map<String, Object> system = new LinkedHashMap<>();
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            system.put("memoryUsed", formatBytes(usedMemory));
            system.put("memoryTotal", formatBytes(totalMemory));
            system.put("memoryMax", formatBytes(maxMemory));
            system.put("memoryUsagePercent", String.format("%.2f%%", (usedMemory * 100.0) / totalMemory));
            system.put("processors", runtime.availableProcessors());
            health.put("system", system);

            // Overall status
            if (!allChecksPass) {
                health.put("status", "DEGRADED");
                log.warn("Health check completed with DEGRADED status");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
            }

            log.info("Health check completed successfully - Status: UP");
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Critical error during health check", e);
            health.put("status", "DOWN");
            health.put("error", "Critical health check failure: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
        }
    }

    // Helper to calculate uptime
    private String getUptimeString() {
        Duration uptime = Duration.between(startTime, LocalDateTime.now());
        long hours = uptime.toHours();
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    // Helper to format bytes into human form
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}


