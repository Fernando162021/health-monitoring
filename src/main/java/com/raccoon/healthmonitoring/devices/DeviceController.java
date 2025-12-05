package com.raccoon.healthmonitoring.devices;

import com.raccoon.healthmonitoring.alerts.AlertService;
import com.raccoon.healthmonitoring.alerts.dto.AlertResponseDto;
import com.raccoon.healthmonitoring.devices.dto.*;
import com.raccoon.healthmonitoring.vitals.dto.VitalRequestDto;
import com.raccoon.healthmonitoring.vitals.dto.VitalResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<DeviceResponseDto>> getDevicesWithLatestVital() {
        List<DeviceResponseDto> devices = deviceService.getDevicesWithLatestVital();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/status")
    public ResponseEntity<List<DeviceWithStatusDto>> getDevicesWithStatus() {
        List<DeviceWithStatusDto> devices = deviceService.getDevicesWithStatus();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{deviceId}/history")
    public ResponseEntity<List<VitalResponseDto>> getDeviceHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "10") int limit) {
        List<VitalResponseDto> history = deviceService.getDeviceHistory(deviceId, limit);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<DeviceResponseDto> registerDevice(
            @Valid @RequestBody DeviceRequestDto deviceRequestDto) {
        DeviceResponseDto device = deviceService.registerDevice(deviceRequestDto);
        return ResponseEntity.ok(device);
    }

    @PostMapping("/data")
    public ResponseEntity<VitalResponseDto> receiveDeviceData(
            @Valid @RequestBody VitalRequestDto vitalRequestDto) {
        VitalResponseDto vital = deviceService.receiveDeviceData(vitalRequestDto);
        return ResponseEntity.ok(vital);
    }

    // Acknowledge all alerts for a device
    @PatchMapping("/{deviceId}/ack")
    public ResponseEntity<List<AlertResponseDto>> acknowledgeAlert(@PathVariable String deviceId) {
        List<AlertResponseDto> acknowledgedAlerts = alertService.acknowledgeAllAlertsForDevice(deviceId);
        return ResponseEntity.ok(acknowledgedAlerts);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
