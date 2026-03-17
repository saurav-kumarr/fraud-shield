package com.fraudshield.transaction.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class TransactionProducer {

    //private static final String TOPIC = "transaction-events";

    @Value("${kafka.topic.transaction-events:transaction-events}")
    private String transactionEventsTopic;

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publishTransaction(TransactionEvent event) {
        log.info("Publishing transaction event: {}", event.getTransactionId());
        kafkaTemplate.send(transactionEventsTopic, event.getTransactionId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Transaction event published successfully: {}" +
                                " partition: {} offset: {}",
                                event.getTransactionId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish transaction event: {}", event.getTransactionId(), ex);
                    }
                });

    }



}
