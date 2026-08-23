package com.fraudshield.transaction.kafka;

import com.fraudshield.transaction.model.Transaction;
import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudAlertConsumer {

    private final TransactionRepository transactionRepository;


    @KafkaListener(topics = "${kafka.topic.fraud-alerts}", groupId = "transaction-service-group")
    @Transactional
    public void handleFraudAlerts(FraudAlertEvent alert) {
        log.info("Received Fraud Alert for Transaction: {} with status: {}",
                alert.getTransactionId(), alert.getFraudStatus());

        Transaction transaction = transactionRepository.findById(alert.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found for ID: " + alert.getTransactionId()));

// SAGA State Machine
        switch (alert.getFraudStatus()) {
            case "APPROVED":
                transaction.setStatus(TransactionStatus.APPROVED);
                break;
            case "BLOCKED":
                transaction.setStatus(TransactionStatus.BLOCKED);
                break;
            case "FLAGGED":
                transaction.setStatus(TransactionStatus.FLAGGED);
                break;
            case "SYSTEM_ERROR": // The SAGA Compensation trigger
                log.warn("SAGA Compensation Triggered: Moving transaction {} to REVIEW_PENDING due to downstream failure.",
                        transaction.getTransactionId());
                transaction.setStatus(TransactionStatus.REVIEW_PENDING);
                break;
            default:
                log.error("Unknown fraud status: {}", alert.getFraudStatus());
        }

        transactionRepository.save(transaction);
        log.info("Transaction {} status updated to {}",
                transaction.getTransactionId(), transaction.getStatus());
    }
}
