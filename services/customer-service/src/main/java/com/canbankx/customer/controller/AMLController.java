package com.canbankx.customer.controller;

import com.canbankx.customer.domain.SuspiciousActivityReport;
import com.canbankx.customer.service.AMLMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/aml")
@RequiredArgsConstructor
public class AMLController {

    private final AMLMonitorService amlSvc;

    @GetMapping("/reports/pending")
    public ResponseEntity<List<SuspiciousActivityReport>> getPendingReports() {
        List<SuspiciousActivityReport> reports = amlSvc.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reports/{reportId}/fintrac")
    public ResponseEntity<String> reportToFintrac(@PathVariable UUID reportId) {
        amlSvc.reportToFintrac(reportId);
        return ResponseEntity.ok("Reported to FINTRAC");
    }
}
