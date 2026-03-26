package com.fraudshield.report.controller;

import com.fraudshield.report.model.FraudReport;
import com.fraudshield.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FraudReport>> getReportsByUserId(
            @PathVariable String userId) {
        log.info("Fetching reports for userId: {}", userId);
        return ResponseEntity.ok(
                reportService.getReportsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FraudReport>> getReportsByStatus(
            @PathVariable String status) {
        log.info("Fetching reports by status: {}", status);
        return ResponseEntity.ok(
                reportService.getReportsByStatus(status));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<FraudReport>> getReportsByMerchantId(
            @PathVariable String merchantId) {
        log.info("Fetching reports for merchantId: {}",
                merchantId);
        return ResponseEntity.ok(
                reportService.getReportsByMerchantId(merchantId));
    }

    @GetMapping("/stats/total-fraud")
    public ResponseEntity<Long> getTotalFraudCount() {
        log.info("Fetching total fraud count");
        return ResponseEntity.ok(
                reportService.getTotalFraudCount());
    }

    @GetMapping("/stats/today-fraud")
    public ResponseEntity<Long> getTodayFraudCount() {
        log.info("Fetching today fraud count");
        return ResponseEntity.ok(
                reportService.getTodayFraudCount());
    }

}
