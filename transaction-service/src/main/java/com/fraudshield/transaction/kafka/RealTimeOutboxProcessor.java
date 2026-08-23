package com.fraudshield.transaction.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.transaction.model.OutboxEventCreated;
import com.fraudshield.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealTimeOutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionProducer transactionProducer;
    private final ObjectMapper objectMapper;

    // This guarantees it ONLY runs if the database transaction committed successfully
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async // Run in a separate thread so we don't block the API response!
    public void processOutboxEvent(OutboxEventCreated event){

        outboxEventRepository.findById(event.outboxEventId()).ifPresent(outboxEvent -> {
            try {
                log.info("Real-time outbox processing for transaction: {}", outboxEvent.getAggregateId());

                TransactionEvent eventPayload = objectMapper.readValue(outboxEvent.getPayload(), TransactionEvent.class);
                transactionProducer.publishTransaction(eventPayload);

                outboxEvent.setProcessed(true);
                outboxEventRepository.save(outboxEvent);

            } catch (Exception e) {
                log.error("Failed to publish outbox event instantly. The fallback sweeper will pick it up.", e);
            }
        });
    }

}
