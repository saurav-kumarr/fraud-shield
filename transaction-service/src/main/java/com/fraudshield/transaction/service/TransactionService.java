package com.fraudshield.transaction.service;

import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.kafka.TransactionEvent;
import com.fraudshield.transaction.kafka.TransactionProducer;
import com.fraudshield.transaction.model.Transaction;
import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionProducer transactionProducer;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {

        log.info("Creating transaction for userId: {}", request.getUserId());

        // Step 1: Build Transaction entity from request
        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
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

        // Step 4: Publish to Kafka
        transactionProducer.publishTransaction(event);

        // Step 5: Return response
        return mapToResponse(savedTransaction);


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
