package com.raccoon.healthmonitoring.thresholds;

import com.raccoon.healthmonitoring.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "thresholds")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Threshold extends BaseEntity {

    @Column(name = "metric", nullable = false, unique = true, length = 50)
    private String metric; // "heartRate", "oxygenLevel", "bodyTemperature"

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "description", length = 200)
    private String description;
}



