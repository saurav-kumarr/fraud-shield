package com.fraudshield.report.service;

import com.fraudshield.report.exception.BadRequestException;
import com.fraudshield.report.model.FraudReport;
import com.fraudshield.report.repository.FraudReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Report Service Tests")
public class ReportServiceTest {

    @Mock
    private FraudReportRepository fraudReportRepository;

    @InjectMocks
    private ReportService reportService;

    private FraudReport report;

    @BeforeEach
    void setUp() {
        report = FraudReport.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .merchantId("merchant-001")
                .fraudStatus("BLOCKED")
                .riskScore(95.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get reports by user ID")
    void shouldGetReportsByUserId() {
        when(fraudReportRepository.findByUserId("user-001"))
                .thenReturn(List.of(report));

        List<FraudReport> reports = reportService
                .getReportsByUserId("user-001");

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getUserId())
                .isEqualTo("user-001");
    }

    @Test
    @DisplayName("Should throw exception when userId is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() ->
                reportService.getReportsByUserId(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User ID is required");
    }

    @Test
    @DisplayName("Should throw exception when userId is empty")
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        assertThatThrownBy(() ->
                reportService.getReportsByUserId(""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User ID is required");
    }

    @Test
    @DisplayName("Should get reports by status")
    void shouldGetReportsByStatus() {
        when(fraudReportRepository
                .findByFraudStatus("BLOCKED"))
                .thenReturn(List.of(report));

        List<FraudReport> reports = reportService
                .getReportsByStatus("BLOCKED");

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getFraudStatus())
                .isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Should throw exception when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        assertThatThrownBy(() ->
                reportService.getReportsByStatus(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Status is required");
    }

    @Test
    @DisplayName("Should get total fraud count")
    void shouldGetTotalFraudCount() {
        when(fraudReportRepository
                .countByFraudStatus("BLOCKED"))
                .thenReturn(50L);

        Long count = reportService.getTotalFraudCount();

        assertThat(count).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should return empty list when no reports")
    void shouldReturnEmptyListWhenNoReports() {
        when(fraudReportRepository.findByUserId("user-999"))
                .thenReturn(List.of());

        List<FraudReport> reports = reportService
                .getReportsByUserId("user-999");

        assertThat(reports).isEmpty();
    }

}
