package com.fraudshield.transaction.dto;

import com.fraudshield.transaction.model.TransactionStatus;
import com.fraudshield.transaction.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private String userId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String deviceId;
    private String ipAddress;
    private String location;
    private TransactionStatus status;
    private TransactionType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
