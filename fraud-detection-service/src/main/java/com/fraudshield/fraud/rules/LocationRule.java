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
public class LocationRule implements FraudRule {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int LOCATION_WINDOW_SECONDS = 3600; // 1 hour
    private static final double RISK_SCORE = 90.0;

    @Override
    public RuleResult evaluate(TransactionEvent transaction) {

        String userId = transaction.getUserId();
        String currentLocation = transaction.getLocation();
        String key = "location:" + userId;

        String lastLocation = redisTemplate.opsForValue().get(key);

        log.info("Location check for userId: {} currentLocation: {} " +
                "lastLocation: {}", userId, currentLocation, lastLocation);

        if(lastLocation != null && !lastLocation.equalsIgnoreCase(currentLocation)) {
            return RuleResult.triggered(
                    getRuleName(),
                    RISK_SCORE,
                    String.format("Location change detected: " +
                                    "last location was %s, current is %s",
                                    lastLocation, currentLocation)
            );
        }

        redisTemplate.opsForValue().set(key,
                currentLocation,
                Duration.ofSeconds(LOCATION_WINDOW_SECONDS));

        return RuleResult.passed(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "LOCATION_RULE";
    }

    @Override
    public int getPriority() {
        return 4;
    }

}
