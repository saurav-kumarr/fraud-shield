package com.fraudshield.alert.controller;

import com.fraudshield.alert.context.UserContext;
import com.fraudshield.alert.exception.BadRequestException;
import com.fraudshield.alert.exception.UnauthorizedException;
import com.fraudshield.alert.model.Alert;
import com.fraudshield.alert.model.AlertStatus;
import com.fraudshield.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
@Slf4j
public class AlertController {

    private final AlertRepository alertRepository;
    private final UserContext userContext;

    @GetMapping("/my-alerts")
    public ResponseEntity<List<Alert>> getMyAlerts() {
        String userId = userContext.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            throw new UnauthorizedException(
                    "User not authenticated");
        }

        String role = userContext.getCurrentUserRole();
        log.info("Fetching alerts for: {} role: {}",
                userId, role);

        if ("ADMIN".equals(role) ||
                "ANALYST".equals(role)) {
            return ResponseEntity.ok(
                    alertRepository.findAll());
        }
        return ResponseEntity.ok(
                alertRepository.findByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Alert>> getAlertsByUserId(
            @PathVariable String userId) {

        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException(
                    "User ID is required");
        }

        log.info("Fetching alerts for userId: {}", userId);
        return ResponseEntity.ok(
                alertRepository.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Alert>> getAlertsByStatus(
            @PathVariable String status) {

        if (!isValidStatus(status)) {
            throw new BadRequestException(
                    "Invalid status. Allowed: APPROVED, " +
                            "BLOCKED, FLAGGED");
        }

        log.info("Fetching alerts by status: {}", status);
        return ResponseEntity.ok(
                alertRepository.findByFraudStatus(status));
    }

    @GetMapping("/alert-status/{alertStatus}")
    public ResponseEntity<List<Alert>> getAlertsByAlertStatus(
            @PathVariable AlertStatus alertStatus) {

        return ResponseEntity.ok(
                alertRepository.findByAlertStatus(alertStatus));
    }

    @GetMapping("/stats/today-fraud")
    public ResponseEntity<Long> getTodayFraudCount() {
        return ResponseEntity.ok(
                alertRepository
                        .countByFraudStatusAndCreatedAtAfter(
                                "BLOCKED",
                                LocalDateTime.now()
                                        .withHour(0)
                                        .withMinute(0)
                                        .withSecond(0)));
    }

    private boolean isValidStatus(String status) {
        return status != null
                &&
                (
                "APPROVED".equals(status) ||
                        "BLOCKED".equals(status) ||
                        "FLAGGED".equals(status));
    }

}
