package com.fraudshield.fraud.rules;

import com.fraudshield.fraud.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Amount Rule Tests")
class AmountRuleTest {

    @InjectMocks
    private AmountRule amountRule;

    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        transaction = TransactionEvent.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .build();
    }

    @Test
    @DisplayName("Should return zero risk for normal amount")
    void shouldReturnZeroRiskForNormalAmount() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("5000"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return zero risk at boundary 50000")
    void shouldReturnZeroRiskAtMediumBoundary() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("50000"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return medium risk just above 50000")
    void shouldReturnMediumRiskJustAboveThreshold() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("50001"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(40.0);
        assertThat(result.getReason())
                .contains("Medium amount");
    }

    @Test
    @DisplayName("Should return medium risk at 75000")
    void shouldReturnMediumRiskForAmount75000() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("75000"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should return medium risk at 100000 boundary")
    void shouldReturnMediumRiskAtHighBoundary() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("100000"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should return high risk just above 100000")
    void shouldReturnHighRiskJustAboveThreshold() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("100001"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(70.0);
        assertThat(result.getReason())
                .contains("High amount");
    }

    @Test
    @DisplayName("Should return high risk for extreme amount")
    void shouldReturnHighRiskForExtremeAmount() {
        // ARRANGE
        transaction.setAmount(new BigDecimal("1000000"));

        // ACT
        RuleResult result = amountRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("Should have AMOUNT_RULE name")
    void shouldReturnCorrectRuleName() {
        assertThat(amountRule.getRuleName())
                .isEqualTo("AMOUNT_RULE");
    }

    @Test
    @DisplayName("Should have priority 3")
    void shouldHavePriorityThree() {
        assertThat(amountRule.getPriority())
                .isEqualTo(3);
    }
}