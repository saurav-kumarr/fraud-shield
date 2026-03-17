package com.fraudshield.alert.repository;

import com.fraudshield.alert.model.Alert;
import com.fraudshield.alert.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert,Long> {

    List<Alert> findByUserId(String userId);

    List<Alert> findByFraudStatus(String fraudStatus);

    List<Alert> findByAlertStatus(AlertStatus alertStatus);

    List<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByFraudStatusAndCreatedAtAfter(String fraudStatus, LocalDateTime after);

}
