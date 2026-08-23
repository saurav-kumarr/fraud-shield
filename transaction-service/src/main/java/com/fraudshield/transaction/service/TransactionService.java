package com.fraudshield.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.exception.BadRequestException;
import com.fraudshield.transaction.exception.ResourceNotFoundException;
import com.fraudshield.transaction.kafka.TransactionEvent;
import com.fraudshield.transaction.kafka.TransactionProducer;
import com.fraudshield.transaction.model.OutboxEvent;
import com.fraudshield.transaction.model.OutboxEventCreated;
import com.fraudshield.transaction.model.Transaction;
import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.repository.OutboxEventRepository;
import com.fraudshield.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    //private final TransactionProducer transactionProducer;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper; // Used to convert object to JSON string
    // 1. INJECT THE PUBLISHER
    private final ApplicationEventPublisher eventPublisher;

    @Transactional  // This now protects BOTH database writes atomically!
    public TransactionResponse createTransaction(TransactionRequest request, String userId) {

        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException(
                    "User ID is required");
        }

        log.info("Creating transaction for userId: {}", userId);

        // Step 1: Build Transaction entity from request
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .type(request.getType())
                .status(TransactionStatus.PENDING)
                .build();

        // Step 2: Save to PostgreSQL
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved with ID: {}", savedTransaction.getTransactionId());

        // Step 3: Build Kafka event
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(savedTransaction.getTransactionId())
                .userId(savedTransaction.getUserId())
                .merchantId(savedTransaction.getMerchantId())
                .amount(savedTransaction.getAmount())
                .currency(savedTransaction.getCurrency())
                .deviceId(savedTransaction.getDeviceId())
                .ipAddress(savedTransaction.getIpAddress())
                .location(savedTransaction.getLocation())
                .type(savedTransaction.getType())
                .timeStamp(LocalDateTime.now())
                .build();


        // Step 4: Save to Outbox (instead of calling Kafka directly)
        OutboxEvent outboxEvent;
        try {
            outboxEvent = OutboxEvent.builder()
                    .aggregateId(savedTransaction.getTransactionId())
                    .eventType("TRANSACTION_CREATED")
                    .payload(objectMapper.writeValueAsString(event))
                    .processed(false)
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("OutboxEvent saved for transactionId: {}", savedTransaction.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to serialize event payload", e);
            throw new RuntimeException("Failed to process transaction event", e);
        }

//        // Step 4: Publish to Kafka
//        transactionProducer.publishTransaction(event);

        // Step 5: SHOUT TO THE SPRING BOOT APP THAT WE SAVED AN OUTBOX EVENT
        eventPublisher.publishEvent(new OutboxEventCreated(outboxEvent.getId()));

        // Step 6: Return response
        return mapToResponse(savedTransaction);


    }

    public List<TransactionResponse> getTransactionsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException(
                    "User ID is required");
        }
        log.info("Fetching transactions for userId: {}", userId);
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(String transactionId) {
        log.info("Fetching transaction ID: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + transactionId));
        return mapToResponse(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {

        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .merchantId(transaction.getMerchantId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .deviceId(transaction.getDeviceId())
                .ipAddress(transaction.getIpAddress())
                .location(transaction.getLocation())
                .status(transaction.getStatus())
                .type(transaction.getType())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();

    }

}
