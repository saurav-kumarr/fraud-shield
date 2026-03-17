package com.fraudshield.fraud.engine;

import com.fraudshield.fraud.dto.TransactionEvent;
import com.fraudshield.fraud.rules.FraudRule;
import com.fraudshield.fraud.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionEngine {

    private final List<FraudRule> fraudRules;

    private static final double BLOCK_THRESHOLD = 70.0;
    private static final double FLAG_THRESHOLD = 40.0;

    public EngineResult analyze(TransactionEvent transaction) {
        log.info("Starting fraud analysis for transactionId: {}",
                transaction.getTransactionId());

//        List<RuleResult> results = fraudRules.stream()
//                .sorted(Comparator.comparingInt(FraudRule::getPriority))
//                .map(rule -> rule.evaluate(transaction))
//                .toList();

        List<RuleResult> results = new ArrayList<>();

        List<FraudRule> sortedRules = fraudRules.stream()
                .sorted(Comparator.comparingInt(FraudRule::getPriority))
                .toList();

        for(FraudRule rule : sortedRules) {
            RuleResult result = rule.evaluate(transaction);
            results.add(result);

            // SHORT_CIRCUIT HERE
            if(result.getRiskScore() >= BLOCK_THRESHOLD) {
                log.info("Short-circuit triggered by rule: {}. Skipping {} remaining rules.",
                        rule.getRuleName(),
                        sortedRules.size() - results.size());
                break;
            }
        }

        double totalScore = results.stream()
                .mapToDouble(RuleResult::getRiskScore)
                .max()
                .orElse(0.0);

        String triggeredReasons = results.stream()
                .filter(RuleResult::isTriggered)
                .map(RuleResult::getReason)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + " | " + b);

        String verdict = determineVerdict(totalScore);

        log.info("Fraud analysis complete for transactionId: {} " +
                        "score: {} verdict: {}",
                transaction.getTransactionId(), totalScore, verdict);

        return EngineResult.builder()
                .transactionId(transaction.getTransactionId())
                .riskScore(totalScore)
                .verdict(verdict)
                .reason(triggeredReasons.isEmpty() ?
                        "All rules passed" : triggeredReasons)
                .ruleResults(results)
                .build();
    }

    private String determineVerdict(double score) {
        if (score >= BLOCK_THRESHOLD) return "BLOCKED";
        if (score >= FLAG_THRESHOLD) return "FLAGGED";
        return "APPROVED";
    }

}
