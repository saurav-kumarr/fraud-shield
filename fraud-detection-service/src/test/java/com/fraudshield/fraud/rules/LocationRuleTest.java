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
@DisplayName("Location Rule Tests")
class LocationRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LocationRule locationRule;

    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        transaction = TransactionEvent.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .location("Delhi, India")
                .amount(new BigDecimal("5000"))
                .build();

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return zero risk for first transaction")
    void shouldReturnZeroRiskForFirstTransaction() {
        // ARRANGE - no previous location
        when(valueOperations.get("location:user-001"))
                .thenReturn(null);

        // ACT
        RuleResult result = locationRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return zero risk for same location")
    void shouldReturnZeroRiskForSameLocation() {
        // ARRANGE - previous same location
        when(valueOperations.get("location:user-001"))
                .thenReturn("Delhi, India");

        // ACT
        RuleResult result = locationRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isZero();
    }

    @Test
    @DisplayName("Should return high risk for location change")
    void shouldReturnHighRiskForLocationChange() {
        // ARRANGE - different previous location
        when(valueOperations.get("location:user-001"))
                .thenReturn("Mumbai, India");

        // ACT
        RuleResult result = locationRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(90.0);
        assertThat(result.getReason())
                .contains("Location");
    }

    @Test
    @DisplayName("Should return high risk for international change")
    void shouldReturnHighRiskForInternationalChange() {
        // ARRANGE - international location change
        when(valueOperations.get("location:user-001"))
                .thenReturn("New York, USA");

        // ACT
        RuleResult result = locationRule.evaluate(transaction);

        // ASSERT
        assertThat(result.getRiskScore()).isEqualTo(90.0);
    }

    @Test
    @DisplayName("Should have LOCATION_RULE name")
    void shouldReturnCorrectRuleName() {
        assertThat(locationRule.getRuleName())
                .isEqualTo("LOCATION_RULE");
    }

    @Test
    @DisplayName("Should have priority 4")
    void shouldHavePriorityFour() {
        assertThat(locationRule.getPriority())
                .isEqualTo(4);
    }
}