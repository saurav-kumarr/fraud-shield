package com.fraudshield.transaction.repository;

import com.fraudshield.transaction.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();

    // NEW METHOD: Delete events that are processed and older than X days
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.processed = true AND e.createdAt < :cutoffDate")
    void deleteProcessedEventsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

}
