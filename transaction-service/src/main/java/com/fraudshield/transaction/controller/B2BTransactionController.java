package com.fraudshield.transaction.controller;

import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class B2BTransactionController {

    private final TransactionService transactionService;

    @PostMapping("/analyze")
    public ResponseEntity<TransactionResponse> analyzeTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-API-Key") String apiKey) {

        log.info("B2B Transaction from API key: {}",
                apiKey.substring(0, 10) + "...");

        TransactionResponse response = transactionService
                .createTransaction(request, "API_" + apiKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TransactionResponse> getStatus(
            @PathVariable String id,
            @RequestHeader("X-API-Key") String apiKey) {

        log.info("B2B Status check for: {}", id);
        return ResponseEntity.ok(
                transactionService.getTransactionById(id));
    }
}