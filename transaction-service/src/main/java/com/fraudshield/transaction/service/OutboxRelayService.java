package com.fraudshield.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.transaction.kafka.TransactionEvent;
import com.fraudshield.transaction.kafka.TransactionProducer;
import com.fraudshield.transaction.model.OutboxEvent;
import com.fraudshield.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionProducer transactionProducer;
    private final ObjectMapper objectMapper;

    // Runs every 5 seconds to check for unpublished events
    @Scheduled(fixedDelayString = "5000")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                log.info("outbox relay processing event for transaction: {}", outboxEvent);

                // Convert JSON string back to TransactionEvent object
                TransactionEvent eventPayload = objectMapper.readValue(outboxEvent.getPayload(), TransactionEvent.class);

                // Try to publish. If Kafka is down, this throws an exception and the event remains unprocessed.
                transactionProducer.publishTransaction(eventPayload);
                // Mark as processed in the database
                outboxEvent.setProcessed(true);
                outboxEventRepository.save(outboxEvent);
            } catch (Exception e) {
                // If Kafka is down, we log the error and move on.
                // The next 5-second interval will try to publish it again.
                log.error("Failed to publish outbox event id: {}", outboxEvent.getId(), e);
            }
        }
    }

}
