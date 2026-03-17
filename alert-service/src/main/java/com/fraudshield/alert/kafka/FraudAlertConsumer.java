package com.fraudshield.alert.kafka;

import com.fraudshield.alert.dto.FraudAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudAlertConsumer {

    private final AlertService alertService;

    @KafkaListener(
            topics = "${kafka.topic.fraud-alerts}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeFraudAlert(FraudAlertEvent event) {
        log.info("Received fraud alert for transactionId: {} " +
                        "status: {} score: {}",
                event.getTransactionId(),
                event.getFraudStatus(),
                event.getRiskScore());

        alertService.processAlert(event);
    }

}
