package com.raccoon.healthmonitoring.alerts;

import com.raccoon.healthmonitoring.common.BaseEntity;
import com.raccoon.healthmonitoring.devices.Device;
import com.raccoon.healthmonitoring.vitals.Vital;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "alerts")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Alert extends BaseEntity {

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_fk")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vital_fk")
    private Vital vital;  // The vital that triggered this alert

    @Column(name = "metric", nullable = false, length = 50)
    private String metric;  // "heartRate", "oxygenLevel", "bodyTemperature"

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "threshold", nullable = false, length = 100)
    private String threshold;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged", nullable = false)
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
}

