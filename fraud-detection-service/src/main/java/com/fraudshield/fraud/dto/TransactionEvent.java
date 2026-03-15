package com.fraudshield.fraud.dto;


import com.fraudshield.fraud.model.TransactionType;
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
public class TransactionEvent {

    private String transactionId;
    private String userId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String deviceId;
    private String ipAddress;
    private String location;
    private TransactionType type;
    private LocalDateTime timeStamp;

}
