package com.raccoon.healthmonitoring.vitals;

import com.raccoon.healthmonitoring.devices.Device;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VitalRepository extends JpaRepository<Vital, Long> {

    @Query("SELECT v FROM Vital v WHERE v.createdAt >= :since ORDER BY v.createdAt DESC")
    List<Vital> findRecentVitals(@Param("since") LocalDateTime since);

    @Query("SELECT v FROM Vital v WHERE v.device = :device ORDER BY v.createdAt DESC")
    List<Vital> findTopNByDevice(@Param("device") Device device, Pageable pageable);
}