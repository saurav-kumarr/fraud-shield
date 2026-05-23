package com.fraudshield.fraud.engine;

import com.fraudshield.fraud.dto.TransactionEvent;
import com.fraudshield.fraud.rules.FraudRule;
import com.fraudshield.fraud.rules.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Fraud Detection Engine Tests")
class FraudDetectionEngineTest {

    @Mock
    private FraudRule blacklistRule;

    @Mock
    private FraudRule velocityRule;

    @Mock
    private FraudRule amountRule;

    @Mock
    private FraudRule locationRule;

    private FraudDetectionEngine engine;
    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        // Setup rule priorities
        when(blacklistRule.getPriority()).thenReturn(1);
        when(velocityRule.getPriority()).thenReturn(2);
        when(amountRule.getPriority()).thenReturn(3);
        when(locationRule.getPriority()).thenReturn(4);

        when(blacklistRule.getRuleName())
                .thenReturn("BLACKLIST_RULE");
        when(velocityRule.getRuleName())
                .thenReturn("VELOCITY_RULE");
        when(amountRule.getRuleName())
                .thenReturn("AMOUNT_RULE");
        when(locationRule.getRuleName())
                .thenReturn("LOCATION_RULE");

        // Create engine with mocked rules
        engine = new FraudDetectionEngine(List.of(
                blacklistRule,
                velocityRule,
                amountRule,
                locationRule
        ));

        transaction = TransactionEvent.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .amount(new BigDecimal("5000"))
                .location("Delhi, India")
                .build();
    }

    @Test
    @DisplayName("Should approve when all rules pass")
    void shouldApproveWhenAllRulesPass() {
        // ARRANGE - all rules return 0 score
        when(blacklistRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("BLACKLIST_RULE"));
        when(velocityRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("VELOCITY_RULE"));
        when(amountRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("AMOUNT_RULE"));
        when(locationRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("LOCATION_RULE"));

        // ACT
        EngineResult result = engine.analyze(transaction);

        // ASSERT
        assertThat(result.getVerdict()).isEqualTo("APPROVED");
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should block when blacklist triggered")
    void shouldBlockWhenBlacklistTriggered() {
        // ARRANGE - blacklist rule returns 100
        when(blacklistRule.evaluate(transaction))
                .thenReturn(RuleResult.triggered(
                        "BLACKLIST_RULE", 100.0,
                        "User blacklisted"));

        // ACT
        EngineResult result = engine.analyze(transaction);

        // ASSERT
        assertThat(result.getVerdict()).isEqualTo("BLOCKED");
        assertThat(result.getRiskScore()).isEqualTo(100.0);

        // Verify short-circuit: other rules NOT called
        verify(velocityRule, never()).evaluate(any());
        verify(amountRule, never()).evaluate(any());
        verify(locationRule, never()).evaluate(any());
    }

    @Test
    @DisplayName("Should flag medium risk transactions")
    void shouldFlagMediumRiskTransactions() {
        // ARRANGE - only amount rule triggers medium
        when(blacklistRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("BLACKLIST_RULE"));
        when(velocityRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("VELOCITY_RULE"));
        when(amountRule.evaluate(transaction))
                .thenReturn(RuleResult.triggered(
                        "AMOUNT_RULE", 40.0,
                        "Medium amount"));
        when(locationRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("LOCATION_RULE"));

        // ACT
        EngineResult result = engine.analyze(transaction);

        // ASSERT
        assertThat(result.getVerdict()).isEqualTo("FLAGGED");
        assertThat(result.getRiskScore()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should use max score not average")
    void shouldUseMaxScoreNotAverage() {
        // ARRANGE - mixed scores
        when(blacklistRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("BLACKLIST_RULE"));
        when(velocityRule.evaluate(transaction))
                .thenReturn(RuleResult.triggered(
                        "VELOCITY_RULE", 80.0,
                        "Velocity exceeded"));
        when(amountRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("AMOUNT_RULE"));
        when(locationRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("LOCATION_RULE"));

        // ACT
        EngineResult result = engine.analyze(transaction);

        // ASSERT - score should be 80 (max), not 20 (average)
        assertThat(result.getRiskScore()).isEqualTo(80.0);
        assertThat(result.getVerdict()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Should execute rules in priority order")
    void shouldExecuteRulesInPriorityOrder() {
        // ARRANGE
        when(blacklistRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("BLACKLIST_RULE"));
        when(velocityRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("VELOCITY_RULE"));
        when(amountRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("AMOUNT_RULE"));
        when(locationRule.evaluate(transaction))
                .thenReturn(RuleResult.passed("LOCATION_RULE"));

        // ACT
        engine.analyze(transaction);

        // ASSERT - verify call order
        var inOrder = inOrder(
                blacklistRule,
                velocityRule,
                amountRule,
                locationRule
        );
        inOrder.verify(blacklistRule).evaluate(transaction);
        inOrder.verify(velocityRule).evaluate(transaction);
        inOrder.verify(amountRule).evaluate(transaction);
        inOrder.verify(locationRule).evaluate(transaction);
    }
}