package com.fraudshield.fraud.rules;

import com.fraudshield.fraud.dto.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class AmountRule implements FraudRule{

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD =
            new BigDecimal("100000");
    private static final BigDecimal MEDIUM_AMOUNT_THRESHOLD =
            new BigDecimal("50000");
    private static final double HIGH_RISK_SCORE = 70.0;
    private static final double MEDIUM_RISK_SCORE = 40.0;

    @Override
    public RuleResult evaluate(TransactionEvent transaction){
        BigDecimal amount = transaction.getAmount();

        log.info("Amount check for transactionId: {} amount: {}",
                transaction.getTransactionId(), amount);

        if(amount.compareTo(HIGH_AMOUNT_THRESHOLD) > 0){
            return RuleResult.triggered(
                    getRuleName(),
                    HIGH_RISK_SCORE,
                    String.format("High amount detected: %.2f exceeds " +
                            "threshold of %.2f", amount, HIGH_AMOUNT_THRESHOLD)
            );
        }

        if(amount.compareTo(MEDIUM_AMOUNT_THRESHOLD) > 0){
            return RuleResult.triggered(
                    getRuleName(),
                    MEDIUM_RISK_SCORE,
                    String.format("Medium amount detected: %.2f exceeds " +
                            "threshold of %.2f",
                            amount, MEDIUM_RISK_SCORE)
            );
        }
        return RuleResult.passed(getRuleName());

    }

    @Override
    public String getRuleName() {
        return "AMOUNT_RULE";
    }

    @Override
    public int getPriority() {
        return 3;
    }

}
