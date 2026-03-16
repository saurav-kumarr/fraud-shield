package com.fraudshield.fraud.rules;

import com.fraudshield.fraud.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlacklistRule implements FraudRule {


    private final RedisTemplate<String, String> redisTemplate;

    private static final double RISK_SCORE = 100.0;

    @Override
    public RuleResult evaluate(TransactionEvent transaction) {
        String userId = transaction.getUserId();
        String merchantId = transaction.getMerchantId();
        String ipAddress = transaction.getIpAddress();

        log.info("Blacklist check for userId: {} merchantId: {} ip: {}",
                userId, merchantId, ipAddress);

        // Check user blacklist
        if (isBlacklisted("blacklist:user:", userId)) {
            return RuleResult.triggered(
                    getRuleName(),
                    RISK_SCORE,
                    "User is blacklisted: " + userId
            );
        }

        // Check merchant blacklist
        if (isBlacklisted("blacklist:merchant:", merchantId)) {
            return RuleResult.triggered(
                    getRuleName(),
                    RISK_SCORE,
                    "Merchant is blacklisted: " + merchantId
            );
        }

        // Check IP blacklist
        if (isBlacklisted("blacklist:ip:", ipAddress)) {
            return RuleResult.triggered(
                    getRuleName(),
                    RISK_SCORE,
                    "IP address is blacklisted: " + ipAddress
            );
        }

        return RuleResult.passed(getRuleName());
    }

    private boolean isBlacklisted(String prefix, String value) {
        String key = prefix + value;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public String getRuleName() {
        return "BLACKLIST_RULE";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
