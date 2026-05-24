package com.fraudshield.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.transaction.context.UserContext;
import com.fraudshield.transaction.dto.TransactionRequest;
import com.fraudshield.transaction.dto.TransactionResponse;
import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.model.TransactionType;
import com.fraudshield.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Controller Tests")
class TransactionControllerTest {


    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserContext userContext;

    private TransactionRequest request;
    private TransactionResponse response;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .build();
        objectMapper = new ObjectMapper();


        request = TransactionRequest.builder()
                .merchantId("merchant-001")
                .amount(new BigDecimal("5000"))
                .currency("INR")
                .deviceId("device-001")
                .ipAddress("192.168.1.1")
                .location("Delhi, India")
                .type(TransactionType.PAYMENT)
                .build();

        response = TransactionResponse.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .merchantId("merchant-001")
                .amount(new BigDecimal("5000"))
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        // ARRANGE
        when(userContext.getCurrentUserId())
                .thenReturn("user-001");
        when(transactionService.createTransaction(
                any(), anyString()))
                .thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId")
                        .value("txn-001"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }

    @Test
    @DisplayName("Should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // ARRANGE - invalid request (no amount)
        TransactionRequest invalidRequest =
                TransactionRequest.builder()
                        .merchantId("merchant-001")
                        .build();

        // ACT & ASSERT
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get my transactions successfully")
    void shouldGetMyTransactionsSuccessfully() throws Exception {
        // ARRANGE
        when(userContext.getCurrentUserId())
                .thenReturn("user-001");
        when(transactionService
                .getTransactionsByUserId("user-001"))
                .thenReturn(List.of(response));

        // ACT & ASSERT
        mockMvc.perform(get(
                        "/api/transactions/my-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId")
                        .value("txn-001"));
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void shouldGetTransactionByIdSuccessfully() throws Exception {
        // ARRANGE
        when(transactionService.getTransactionById("txn-001"))
                .thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(get("/api/transactions/txn-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId")
                        .value("txn-001"));
    }

    @Test
    @DisplayName("Should get transactions by user ID")
    void shouldGetTransactionsByUserId() throws Exception {
        // ARRANGE
        when(transactionService
                .getTransactionsByUserId("user-001"))
                .thenReturn(List.of(response));

        // ACT & ASSERT
        mockMvc.perform(get(
                        "/api/transactions/user/user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId")
                        .value("user-001"));
    }
}