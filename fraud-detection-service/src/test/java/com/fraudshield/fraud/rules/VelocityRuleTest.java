package com.fraudshield.fraud.rules;


import com.fraudshield.fraud.dto.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Velocity Rule Tests")
public class VelocityRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private VelocityRule velocityRule;

    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        // ARRANGE - common setup
        transaction = TransactionEvent.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .amount(new BigDecimal("5000"))
                .build();

        // Mock opsForValue chain
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return zero risk " +
            "for first transaction")
    void shouldReturnZeroRiskForFirstTransaction() {
        // ARRANGE
        when(valueOperations.increment("velocity:user-001"))
                .thenReturn(1L);

        // ACT
        RuleResult result = velocityRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return zero risk " +
            "when count is within threshold")
    void shouldReturnZeroRiskWhenCountWithinThreshold() {
        // ARRANGE
        when(valueOperations.increment("velocity:user-001"))
                .thenReturn(3L);

        // ACT
        RuleResult result = velocityRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return zero risk " +
            "exactly at threshold")
    void shouldReturnZeroRiskAtThreshold() {
        // ARRANGE
        when(valueOperations.increment("velocity:user-001"))
                .thenReturn(5L);

        // ACT
        RuleResult result = velocityRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return high risk " +
            "when velocity exceeds threshold")
    void shouldReturnHighRiskWhenVelocityExceeded() {
        // ARRANGE
        when(valueOperations.increment("velocity:user-001"))
                .thenReturn(6L);

        // ACT
        RuleResult result = velocityRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(80.0);
        assertThat(result.getReason())
                .contains("Velocity");
    }

    @Test
    @DisplayName("Should return high risk " +
            "for very high velocity")
    void shouldReturnHighRiskForVeryHighVelocity() {
        // ARRANGE
        when(valueOperations.increment("velocity:user-001"))
                .thenReturn(100L);

        // ACT
        RuleResult result = velocityRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Should have VELOCITY_RULE name")
    void shouldReturnCorrectRuleName() {
        assertThat(velocityRule.getRuleName())
                .isEqualTo("VELOCITY_RULE");
    }

    @Test
    @DisplayName("Should have priority 2")
    void shouldHavePriorityTwo() {
        assertThat(velocityRule.getPriority())
                .isEqualTo(2);
    }


}
