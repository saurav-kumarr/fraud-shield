package com.fraudshield.fraud.kafka;

import com.fraudshield.fraud.dto.FraudAlertEvent;
import com.fraudshield.fraud.dto.TransactionEvent;
import com.fraudshield.fraud.engine.EngineResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final FraudAlertProducer fraudAlertProducer;

    @KafkaListener(
            topics = "${kafka.topic.transaction-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {}",
                event.getTransactionId());

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
    }

}
