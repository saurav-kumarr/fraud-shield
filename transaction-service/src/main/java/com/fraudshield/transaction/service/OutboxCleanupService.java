package com.fraudshield.transaction.service;

import com.fraudshield.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxCleanupService {

    private final OutboxEventRepository outboxEventRepository;

    // Runs every day at 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupProcessedOutboxEvents() {
        log.info("Starting cleanup of old processed outbox events...");

        // Calculate the date 7 days ago
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // Delete them from the database
        outboxEventRepository.deleteProcessedEventsOlderThan(cutoffDate);

        log.info("Finished cleanup of processed outbox events older than {}", cutoffDate);
    }
}

