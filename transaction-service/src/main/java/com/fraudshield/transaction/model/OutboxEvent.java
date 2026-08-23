package com.fraudshield.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvent {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String aggregateId;   // The Transaction ID

    @Column(nullable = false)
    private String eventType;   // e.g., "TRANSACTION_CREATED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;  // The JSON representation of TransactionEven

    @Column(nullable = false)
    private boolean processed;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
