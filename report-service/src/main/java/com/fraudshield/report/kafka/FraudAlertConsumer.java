package com.fraudshield.report.kafka;

import com.fraudshield.report.dto.FraudAlertEvent;
import com.fraudshield.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudAlertConsumer {

    private final ReportService reportService;

    @KafkaListener(
            topics = "${kafka.topic.fraud-alerts}",
            groupId = "${spring.kafka.consumer.group-id}"
    )

    public void consumerFraudAlert(FraudAlertEvent event) {
        log.info("Received fraud alert for report: {} " +
                "status: {} score: {}",
                event.getTransactionId(),
                event.getFraudStatus(),
                event.getRiskScore());

        reportService.generateReport(event);
    }

}
