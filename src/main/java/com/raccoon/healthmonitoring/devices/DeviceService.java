package com.raccoon.healthmonitoring.devices;

import com.raccoon.healthmonitoring.alerts.AlertService;
import com.raccoon.healthmonitoring.devices.dto.*;
import com.raccoon.healthmonitoring.graphql.DeviceGraphQLHandler;
import com.raccoon.healthmonitoring.thresholds.ThresholdService;
import com.raccoon.healthmonitoring.vitals.Vital;
import com.raccoon.healthmonitoring.vitals.VitalMapper;
import com.raccoon.healthmonitoring.vitals.VitalRepository;
import com.raccoon.healthmonitoring.vitals.dto.VitalRequestDto;
import com.raccoon.healthmonitoring.vitals.dto.VitalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final VitalRepository vitalRepository;
    private final VitalMapper vitalMapper;
    private final ThresholdService thresholdService;
    private final AlertService alertService;

    public List<DeviceResponseDto> getDevicesWithLatestVital() {
        return deviceRepository.findAllByIsActiveTrue().stream()
                .map(device -> {
                    Vital latestVital = device.getLastVital();
                    VitalResponseDto latestVitalDto = latestVital != null ? vitalMapper.toVitalResponseDto(latestVital) : null;
                    return new DeviceResponseDto(device.getDeviceId(), device.getIsActive(), latestVitalDto);
                })
                .toList();
    }

    public DeviceResponseDto registerDevice(
            DeviceRequestDto deviceRequestDto
    ) {
        if (deviceRepository.findByDeviceId(deviceRequestDto.deviceId()).isPresent()) {
            throw new IllegalArgumentException("Device already registered: " + deviceRequestDto.deviceId());
        }

        Device device = Device.builder()
                .deviceId(deviceRequestDto.deviceId())
                .isActive(true)
                .build();
        Device savedDevice = deviceRepository.save(device);
        return deviceMapper.toDeviceResponseDto(savedDevice);
    }

    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setIsActive(false);
        deviceRepository.save(device);
    }

    public VitalResponseDto receiveDeviceData(
            VitalRequestDto vitalRequestDto
    ) {
        Device device = deviceRepository.findByDeviceId(vitalRequestDto.deviceId())
                .filter(Device::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or inactive: " + vitalRequestDto.deviceId()));

        Vital vital = Vital.builder()
                .deviceId(vitalRequestDto.deviceId())
                .heartRate(vitalRequestDto.heartRate())
                .oxygenLevel(vitalRequestDto.oxygenLevel())
                .bodyTemperature(vitalRequestDto.bodyTemperature())
                .steps(vitalRequestDto.steps())
                .device(device)
                .build();

        Vital saved = vitalRepository.save(vital);
        device.setLastVital(saved);
        deviceRepository.save(device);

        // Create alerts automatically if vital is out of range
        alertService.createAlertsFromVital(saved, device);

        // Emit event for GraphQL subscription (using createdAt from BaseEntity)
        DeviceGraphQLHandler.publishVital(saved);
        return vitalMapper.toVitalResponseDto(saved);
    }

    // Returns: deviceId, status (OK/ALERT), HR, SpO₂, last update
    public List<DeviceWithStatusDto> getDevicesWithStatus() {
        return deviceRepository.findAllByIsActiveTrue().stream()
                .map(device -> {
                    Vital lastVital = device.getLastVital();

                    if (lastVital == null) {
                        return new DeviceWithStatusDto(
                                device.getDeviceId(),
                                "NO_DATA",
                                null, null, null, null, null
                        );
                    }

                    String status = isVitalOutOfRange(lastVital) ? "ALERT" : "OK";

                    return new DeviceWithStatusDto(
                            device.getDeviceId(),
                            status,
                            lastVital.getHeartRate(),
                            lastVital.getOxygenLevel(),
                            lastVital.getBodyTemperature(),
                            lastVital.getSteps(),
                            lastVital.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // Last N readings for a device (for drill-down modal)
    public List<VitalResponseDto> getDeviceHistory(String deviceId, int limit) {

        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("Device not found: " + deviceId);
                });

        Pageable pageable = PageRequest.of(0, limit);

        return vitalRepository.findTopNByDevice(device, pageable)
                .stream()
                .map(vitalMapper::toVitalResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isVitalOutOfRange(Vital vital) {
        return thresholdService.isVitalOutOfRange(vital);
    }
}



