package com.raccoon.healthmonitoring.alerts;

import com.raccoon.healthmonitoring.alerts.dto.AlertResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Get unacknowledged alerts
    @GetMapping("/active")
    public ResponseEntity<List<AlertResponseDto>> getActiveAlerts() {
        List<AlertResponseDto> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    // Get alert history (last N hours)
    @GetMapping("/history")
    public ResponseEntity<List<AlertResponseDto>> getAlertHistory(
            @RequestParam(defaultValue = "24") int hours) {
        List<AlertResponseDto> alerts = alertService.getAlertHistory(hours);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<AlertResponseDto>> getAlertsByDevice(
            @PathVariable String deviceId) {
        List<AlertResponseDto> alerts = alertService.getAlertsByDevice(deviceId);
        return ResponseEntity.ok(alerts);
    }
}

