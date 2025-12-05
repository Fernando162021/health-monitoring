package com.raccoon.healthmonitoring.vitals;

import com.raccoon.healthmonitoring.common.BaseEntity;
import com.raccoon.healthmonitoring.devices.Device;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vitals")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Vital extends BaseEntity {
    /**
     * Device identifier that sent this reading (String, not FK)
     * Example: "P-102", "P-103"
     */
    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "heart_rate", nullable = false)
    private Double heartRate;

    @Column(name = "oxygen_level", nullable = false)
    private Double oxygenLevel;

    @Column(name = "body_temperature", nullable = false)
    private Double bodyTemperature;

    @Column(nullable = false)
    private Integer steps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_fk")
    private Device device;
}
