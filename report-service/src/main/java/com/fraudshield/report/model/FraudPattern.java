package com.fraudshield.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fraud_patterns")
public class FraudPattern {

    @Id
    private String id;

    private String patternType;
    private String userId;
    private String location;
    private double riskScore;
    private String riskReason;
    private String fraudStatus;
    private LocalDateTime detectedAt;
}
