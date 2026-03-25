package com.fraudshield.report.service;

import com.fraudshield.report.dto.FraudAlertEvent;
import com.fraudshield.report.model.FraudPattern;
import com.fraudshield.report.model.FraudReport;
import com.fraudshield.report.repository.FraudPatternRepository;
import com.fraudshield.report.repository.FraudReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private final FraudReportRepository fraudReportRepository;
    private final FraudPatternRepository fraudPatternRepository;

    public void generateReport(FraudAlertEvent event) {

        log.info("Generating report for transactionId: {}",
                event.getTransactionId());


        // Step 1: Save to PostgreSQL
        saveFraudReport(event);

        // Step 2: Save pattern to MongoDB
        // Only save if BLOCKED or FLAGGED
        if (!"APPROVED".equals(event.getFraudStatus())) {
            saveFraudPattern(event);
        }

        log.info("Report generated for transactionId: {}",
                event.getTransactionId());

    }

    private void saveFraudReport(FraudAlertEvent event) {
        FraudReport report = FraudReport.builder()
                .transactionId(event.getTransactionId())
                .userId(event.getUserId())
                .merchantId(event.getMerchantId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .location(event.getLocation())
                .fraudStatus(event.getFraudStatus())
                .riskScore(event.getRiskScore())
                .riskReason(event.getRiskReason())
                .build();

        fraudReportRepository.save(report);
        log.info("Fraud report saved for transactionId: {}",
                event.getTransactionId());
    }

    private void saveFraudPattern(FraudAlertEvent event) {
        FraudPattern pattern = FraudPattern.builder()
                .userId(event.getUserId())
                .location(event.getLocation())
                .riskScore(event.getRiskScore())
                .riskReason(event.getRiskReason())
                .fraudStatus(event.getFraudStatus())
                .detectedAt(event.getDetectedAt())
                .patternType(determinePatternType(
                        event.getRiskReason()))
                .build();

        fraudPatternRepository.save(pattern);
        log.info("Fraud pattern saved for userId: {}",
                event.getUserId());
    }

    private String determinePatternType(String riskReason) {
        if (riskReason == null) return "UNKNOWN";
        if (riskReason.contains("Velocity"))
            return "VELOCITY";
        if (riskReason.contains("amount") ||
                riskReason.contains("Amount"))
            return "HIGH_AMOUNT";
        if (riskReason.contains("Location") ||
                riskReason.contains("location"))
            return "LOCATION";
        if (riskReason.contains("blacklisted"))
            return "BLACKLIST";
        return "UNKNOWN";
    }

    public List<FraudReport> getReportsByUserId(
            String userId) {
        return fraudReportRepository.findByUserId(userId);
    }

    public List<FraudReport> getReportsByStatus(
            String status) {
        return fraudReportRepository
                .findByFraudStatus(status);
    }

    public List<FraudReport> getReportsByMerchantId(
            String merchantId) {
        return fraudReportRepository
                .findByMerchantId(merchantId);
    }

    public long getTotalFraudCount() {
        return fraudReportRepository
                .countByFraudStatus("BLOCKED");
    }

    public long getTodayFraudCount() {
        return fraudReportRepository
                .countByFraudStatusAndCreatedAtAfter(
                        "BLOCKED",
                        LocalDateTime.now().withHour(0)
                                .withMinute(0)
                                .withSecond(0));
    }


}
