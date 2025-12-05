package com.raccoon.healthmonitoring.thresholds;

import com.raccoon.healthmonitoring.thresholds.dto.ThresholdResponseDto;
import com.raccoon.healthmonitoring.thresholds.dto.ThresholdUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class ThresholdController {

    private final ThresholdService thresholdService;

    @GetMapping
    public ResponseEntity<List<ThresholdResponseDto>> getAllThresholds() {
        List<ThresholdResponseDto> thresholds = thresholdService.getAllThresholds();
        return ResponseEntity.ok(thresholds);
    }

    @PutMapping
    public ResponseEntity<ThresholdResponseDto> updateThresholdValues(
            @Valid @RequestBody ThresholdUpdateDto thresholdUpdateDto
    ) {
        ThresholdResponseDto updated = thresholdService.updateThresholdValues(thresholdUpdateDto);
        return ResponseEntity.ok(updated);
    }
}

