package com.fraudshield.alert.dto;

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
public class FraudAlertEvent {

    private String transactionId;
    private String userId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String location;
    private String fraudStatus;
    private double riskScore;
    private String riskReason;
    private LocalDateTime detectedAt;

}
