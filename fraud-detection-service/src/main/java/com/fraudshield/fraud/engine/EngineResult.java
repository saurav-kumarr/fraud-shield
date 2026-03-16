package com.fraudshield.fraud.engine;

import com.fraudshield.fraud.rules.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EngineResult {

    private String transactionId;
    private double riskScore;
    private String verdict;
    private String reason;
    private List<RuleResult> ruleResults;

}
