package com.fraudshield.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final long TTL_HOURS = 24;

    /**
     * Checks if the idempotency key already exists.
     * @return true if it's a duplicate, false if it is a new request.
     */

    public boolean isDuplicate(String idempotencyKey) {
        String redisKey = "idemp:txn:" + idempotencyKey;

        // SETNX: Sets the key only if it does not already exist
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSED", Duration.ofHours(TTL_HOURS));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate request intercepted for Idempotency-Key: {}", idempotencyKey);
            return true;
        }
        return false;
    }

}
