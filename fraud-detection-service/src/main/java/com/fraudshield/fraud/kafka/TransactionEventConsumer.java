package com.fraudshield.fraud.kafka;

import com.fraudshield.fraud.dto.FraudAlertEvent;
import com.fraudshield.fraud.dto.TransactionEvent;
import com.fraudshield.fraud.engine.EngineResult;
import com.fraudshield.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final FraudAlertProducer fraudAlertProducer;
    private final StringRedisTemplate redisTemplate; // Added for Consumer Idempotency

    @KafkaListener(
            topics = "${kafka.topic.transaction-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {} on thread: {}",
                event.getTransactionId(), Thread.currentThread().getName());

        // 1. CONSUMER IDEMPOTENCY CHECK
        // If Kafka sends this exact transaction ID again, Redis blocks it.
        String redisKey = "idemp:fraud:txn:" + event.getTransactionId();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", Duration.ofDays(7));

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate Kafka message detected. Skipping fraud check for transaction: {}", event.getTransactionId());
            return;
        }

        try {
            // 2. NORMAL SAGA PROCESSING

            EngineResult result = fraudDetectionService.analyzeTransaction(event);

            FraudAlertEvent alertEvent = FraudAlertEvent.builder()
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .merchantId(event.getMerchantId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .location(event.getLocation())
                    .fraudStatus(result.getVerdict())
                    .riskScore(result.getRiskScore())
                    .riskReason(result.getReason())
                    .detectedAt(LocalDateTime.now())
                    .build();

            fraudAlertProducer.publishFraudAlert(alertEvent);

            log.info("Fraud analysis complete for transactionId: {} " +
                            "verdict: {} score: {}",
                    event.getTransactionId(),
                    result.getVerdict(),
                    result.getRiskScore());

        } catch (Exception e) {

            // 3. SAGA COMPENSATING TRANSACTION
            log.error("CRITICAL: Fraud Engine crashed for transaction: {}. Emitting Compensating Event.", event.getTransactionId(), e);

            FraudAlertEvent failureEvent = FraudAlertEvent.builder()
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .merchantId(event.getMerchantId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .location(event.getLocation())
                    .fraudStatus("SYSTEM_ERROR") // The special compensation flag
                    .riskScore(0.0)
                    .riskReason("Fraud Engine System Failure: " + e.getMessage())
                    .detectedAt(LocalDateTime.now())
                    .build();

            fraudAlertProducer.publishFraudAlert(failureEvent);
        }
    }

}
