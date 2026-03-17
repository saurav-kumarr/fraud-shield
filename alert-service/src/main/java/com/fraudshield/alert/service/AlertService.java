package com.fraudshield.alert.service;


import com.fraudshield.alert.dto.FraudAlertEvent;
import com.fraudshield.alert.model.Alert;
import com.fraudshield.alert.model.AlertStatus;
import com.fraudshield.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void processAlert(FraudAlertEvent event){
        log.info("Processing alert for transactionId: {} status: {}",
                event.getTransactionId(),
                event.getFraudStatus());

        // Step 1: Save alert to PostgreSQL
        Alert alert = saveAlert(event);

        sendWebSocketAlert(event);

        // Step 3: Send email if BLOCKED
        if("BLOCKED".equals(event.getFraudStatus())) {
            sendEmailAlert(event);
        }

        log.info("Alert processed successsfully for transactionId: {}",
                event.getTransactionId());
    }

    private Alert saveAlert(FraudAlertEvent event) {
        Alert alert = Alert.builder()
                .transactionId(event.getTransactionId())
                .userId(event.getUserId())
                .merchantId(event.getMerchantId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .location(event.getLocation())
                .fraudStatus(event.getFraudStatus())
                .riskScore(event.getRiskScore())
                .riskReason(event.getRiskReason())
                .alertStatus(AlertStatus.PENDING)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert saved with id: {}", savedAlert.getId());
        return savedAlert;
    }

    private void sendWebSocketAlert(FraudAlertEvent event) {
        log.info("Sending WebSocket alert for transactionId: {}",
                event.getTransactionId());
        // WebSocket implementation coming next
        simpMessagingTemplate.convertAndSend("/topic/fraud-alerts", event);
        log.info("WebSocket alert sent for transactionId: {}", event.getTransactionId());
    }

    private void sendEmailAlert(FraudAlertEvent event) {
        log.info("Sending email alert for transactionId: {}",
                event.getTransactionId());

        // Email implementation coming next
    }
}
