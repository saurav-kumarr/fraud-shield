package com.fraudshield.fraud.rules;

import com.fraudshield.fraud.dto.TransactionEvent;
//import com.fraudshield.fraud.rules.RuleResult;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Blacklist Rule Tests")
class BlacklistRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private BlacklistRule blacklistRule;

    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        transaction = TransactionEvent.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .merchantId("merchant-001")
                .ipAddress("192.168.1.1")
                .amount(new BigDecimal("5000"))
                .build();
    }

    @Test
    @DisplayName("Should return high risk score " +
            "when user is blacklisted")
    void shouldReturnHighRiskScoreWhenUserBlacklisted() {
        // Given
        when(redisTemplate.hasKey("blacklist:user:user-001"))
                .thenReturn(true);
//        when(redisTemplate.hasKey("blacklist:merchant:merchant-001"))
//                .thenReturn(false);
//        when(redisTemplate.hasKey("blacklist:ip:192.168.1.1"))
//                .thenReturn(false);

        // When
        RuleResult result = blacklistRule.evaluate(transaction);

        // Then
        assertThat(result.getRiskScore())
                .isEqualTo(100.0);
        assertThat(result.getReason())
                .contains("blacklisted");
    }

    @Test
    @DisplayName("Should return zero risk when " +
            "nothing is blacklisted")
    void shouldReturnZeroRiskWhenNothingBlacklisted() {
        // Given
        when(redisTemplate.hasKey("blacklist:user:user-001"))
                .thenReturn(false);
        when(redisTemplate.hasKey("blacklist:merchant:merchant-001"))
                .thenReturn(false);
        when(redisTemplate.hasKey("blacklist:ip:192.168.1.1"))
                .thenReturn(false);

        // When
        RuleResult result = blacklistRule.evaluate(transaction);

        // Then
        assertThat(result.getRiskScore())
                .isZero();
    }

    @Test
    @DisplayName("Should return BLACKLIST_RULE as name")
    void shouldReturnCorrectRuleName() {
        assertThat(blacklistRule.getRuleName())
                .isEqualTo("BLACKLIST_RULE");
    }

    @Test
    @DisplayName("Should have priority 1")
    void shouldHavePriorityOne() {
        assertThat(blacklistRule.getPriority())
                .isEqualTo(1);
    }
}