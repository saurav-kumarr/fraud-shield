package com.fraudshield.report.repository;

import com.fraudshield.report.model.FraudReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudReportRepository extends JpaRepository<FraudReport, Long> {


    List<FraudReport> findByUserId(String userId);

    List<FraudReport> findByFraudStatus(String fraudStatus);

    List<FraudReport> findByMerchantId(String merchantId);

    List<FraudReport> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByFraudStatus(String fraudStatus);

    long countByFraudStatusAndCreatedAtAfter(
            String fraudStatus,
            LocalDateTime after
    );

}
