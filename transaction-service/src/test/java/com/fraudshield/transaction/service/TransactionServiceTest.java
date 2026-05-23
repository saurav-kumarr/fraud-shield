package com.fraudshield.transaction.service;


import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.exception.BadRequestException;
import com.fraudshield.transaction.exception.ResourceNotFoundException;
import com.fraudshield.transaction.kafka.TransactionProducer;
import com.fraudshield.transaction.model.Transaction;
import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.model.TransactionType;
import com.fraudshield.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Tests")
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionProducer transactionProducer;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest request;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        request = TransactionRequest.builder()
                .merchantId("merchant-001")
                .amount(new BigDecimal("5000"))
                .currency("INR")
                .deviceId("device-001")
                .ipAddress("192.168.1.1")
                .location("Delhi, India")
                .type(TransactionType.PAYMENT)
                .build();

        transaction = Transaction.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .merchantId("merchant-001")
                .amount(new BigDecimal("5000"))
                .currency("INR")
                .deviceId("device-001")
                .ipAddress("192.168.1.1")
                .location("Delhi, India")
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        // ARRANGE
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        // ACT
        TransactionResponse response = transactionService
                .createTransaction(request, "user-001");

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("user-001");
        assertThat(response.getAmount())
                .isEqualTo(new BigDecimal("5000"));
        assertThat(response.getStatus())
                .isEqualTo(TransactionStatus.PENDING);

        // Verify Kafka event published
        verify(transactionProducer).publishTransaction(any());
    }

    @Test
    @DisplayName("Should throw exception when userId is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // ACT & ASSERT
        assertThatThrownBy(() ->
                transactionService.createTransaction(
                        request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User ID is required");

        // Verify nothing saved or published
        verify(transactionRepository, never())
                .save(any());
        verify(transactionProducer, never())
                .publishTransaction(any());
    }

    @Test
    @DisplayName("Should throw exception when userId is empty")
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        assertThatThrownBy(() ->
                transactionService.createTransaction(
                        request, ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User ID is required");
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void shouldGetTransactionByIdSuccessfully() {
        // ARRANGE
        when(transactionRepository.findById("txn-001"))
                .thenReturn(Optional.of(transaction));

        // ACT
        TransactionResponse response = transactionService
                .getTransactionById("txn-001");

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getTransactionId())
                .isEqualTo("txn-001");
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // ARRANGE
        when(transactionRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() ->
                transactionService.getTransactionById(
                        "invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Transaction not found");
    }

    @Test
    @DisplayName("Should get transactions by userId")
    void shouldGetTransactionsByUserId() {
        // ARRANGE
        when(transactionRepository.findByUserId("user-001"))
                .thenReturn(List.of(transaction));

        // ACT
        List<TransactionResponse> responses = transactionService
                .getTransactionsByUserId("user-001");

        // ASSERT
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId())
                .isEqualTo("user-001");
    }

    @Test
    @DisplayName("Should return empty list when no transactions")
    void shouldReturnEmptyListWhenNoTransactions() {
        // ARRANGE
        when(transactionRepository.findByUserId("user-999"))
                .thenReturn(List.of());

        // ACT
        List<TransactionResponse> responses = transactionService
                .getTransactionsByUserId("user-999");

        // ASSERT
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when getTransactions userId null")
    void shouldThrowExceptionWhenGetTransactionsUserIdNull() {
        assertThatThrownBy(() ->
                transactionService.getTransactionsByUserId(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User ID is required");
    }

}
