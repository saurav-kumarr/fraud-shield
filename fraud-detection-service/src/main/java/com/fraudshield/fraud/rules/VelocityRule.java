package com.fraudshield.fraud.rules;

import com.fraudshield.fraud.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class VelocityRule implements FraudRule{

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_TRANSACTIONS = 5;
    private static final int WINDOW_SECONDS = 600;
    private static final double RISK_SCORE = 80.0;

    @Override
    public RuleResult evaluate(TransactionEvent transaction){
        String key = "velocity:" + transaction.getUserId();
        Long count = redisTemplate.opsForValue().increment(key);

        if(count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        log.info("Velocity check for userId: {} count: {}", transaction.getUserId(), count);

        if(count > MAX_TRANSACTIONS) {
            return RuleResult.triggered(
                    getRuleName(),
                    RISK_SCORE,
                    String.format("Velocity exceeded: %d transactions " + "in last 10 minutes", count)
            );
        }
        return RuleResult.passed(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "VELOCITY_RILE";
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
