package com.fraudshield.fraud.kafka;


import com.fraudshield.fraud.dto.FraudAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudAlertProducer {

    private final KafkaTemplate<String, FraudAlertEvent> kafkaTemplate;

    @Value("${kafka.topic.fraud-alerts}")
    private String fraudAlertsTopic;

    public void publishFraudAlert(FraudAlertEvent event) {
        log.info("Publishing fraud alert for transactionId: {}",
                event.getTransactionId());

        kafkaTemplate.send(
                fraudAlertsTopic,
                event.getTransactionId(),
                event)
                .whenComplete((result, ex) -> {
                    if(ex == null) {
                        log.info("Fraud alert published successfully: {}" +
                                        " partition: {} offset: {}",
                                event.getTransactionId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish fraud alert: {}",
                                event.getTransactionId(), ex);
                    }
                });
    }

}
