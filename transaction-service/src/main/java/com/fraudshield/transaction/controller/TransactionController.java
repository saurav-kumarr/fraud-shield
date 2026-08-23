package com.fraudshield.transaction.controller;

import com.fraudshield.transaction.context.UserContext;
import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.exception.DuplicateTransactionException;
import com.fraudshield.transaction.service.IdempotencyService;
import com.fraudshield.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader("Idempotency-Key")  String idempotencyKey,
            @Valid @RequestBody TransactionRequest request
            ){

        // 1. Check Idempotency immediately
        if (idempotencyService.isDuplicate(idempotencyKey)) {
            throw new DuplicateTransactionException("Duplicate request detected for key: " + idempotencyKey);
        }

        String userId = userContext.getCurrentUserId();
        log.info("Received transaction request for userId: {}", userId);
        TransactionResponse response = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable String transactionId) {
        log.info("Fetching transaction by ID: {}", transactionId);
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(@PathVariable String userId){

        log.info("Fetching all transactions for userId: {}", userId);
        List<TransactionResponse> response = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-transactions")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        String userId = userContext.getCurrentUserId();
        log.info("Fetching transactions for: {}", userId);
        return ResponseEntity.ok(
                transactionService.getTransactionsByUserId(userId));
    }

}
