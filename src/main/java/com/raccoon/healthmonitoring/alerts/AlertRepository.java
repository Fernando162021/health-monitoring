package com.raccoon.healthmonitoring.alerts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAcknowledgedFalseOrderByTriggeredAtDesc();
    List<Alert> findByDeviceIdOrderByTriggeredAtDesc(String deviceId);
    @Query("SELECT a FROM Alert a WHERE a.triggeredAt >= :since ORDER BY a.triggeredAt DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);
    List<Alert> findByDeviceIdAndAcknowledgedFalseOrderByTriggeredAtDesc(String deviceId);
}