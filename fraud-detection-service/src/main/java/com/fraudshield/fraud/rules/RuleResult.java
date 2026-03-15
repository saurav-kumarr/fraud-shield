package com.fraudshield.fraud.rules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleResult {

    private String ruleName;
    private double riskScore;
    private boolean triggered;
    private String reason;

    public static RuleResult passed(String ruleName){
        return RuleResult.builder()
                .ruleName(ruleName)
                .riskScore(0)
                .triggered(false)
                .reason("Rule passed")
                .build();
    }

    public static RuleResult triggered(String ruleName, double riskScore, String reason){
        return RuleResult.builder()
                .ruleName(ruleName)
                .riskScore(riskScore)
                .triggered(true)
                .reason(reason)
                .build();

    }

}
