package com.raccoon.healthmonitoring.reports;

import com.raccoon.healthmonitoring.reports.dto.ReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportDto> getReport(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "5") Integer window
    ) {
        ReportDto report = reportService.getReport(deviceId, window);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportReport(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "5") Integer window) {
        String csv = reportService.exportReportAsCSV(deviceId, window);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "report-" + deviceId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
}

