package com.raccoon.healthmonitoring.devices;

import com.raccoon.healthmonitoring.common.BaseEntity;
import com.raccoon.healthmonitoring.vitals.Vital;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "devices")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Device extends BaseEntity {
    /**
     * Business identifier for the device (e.g., "P-102")
     * This is what Node-RED sends in the payload
     */
    @Column(name = "device_id", nullable = false, unique = true, length = 50)
    private String deviceId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_vital_id")
    private Vital lastVital;

    // Relationship: Device has many Vitals (historical readings)
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Vital> vitals = new ArrayList<>();
}
