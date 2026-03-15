package com.fraudshield.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "fraud_patterns")
public class FraudPattern {

    @Id
    private String id;

    private String patternType;
    private String description;
    private double riskWeight;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
