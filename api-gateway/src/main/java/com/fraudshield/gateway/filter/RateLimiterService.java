package com.fraudshield.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimiterService {

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, String role) {
        return buckets.computeIfAbsent(key, k -> createBucket(role));
    }

    private Bucket createBucket(String role) {
        return switch (role) {
            case "ADMIN" -> Bucket.builder()
                    // Secondary Burst limit for per Second
                    .addLimit(Bandwidth.builder()
                            .capacity(50)
                            .refillIntervallyAligned(50,
                                    Duration.ofSeconds(1),
                                    Instant.now())
                            .build())

                    // Primary Sustained limit for per Minute
                    .addLimit(Bandwidth.builder()
                            .capacity(10000)
                            .refillIntervallyAligned(10000,
                                    Duration.ofMinutes(1),
                                    Instant.now())
                            .build())
                    .build();

            case "MERCHANT" -> Bucket.builder()

                    // Secondary Burst limit for per Second
                    .addLimit(Bandwidth.builder()
                            .capacity(20)
                            .refillIntervallyAligned(20,
                                    Duration.ofSeconds(1),
                                    Instant.now())
                            .build())

                    // Primary Sustained limit for per Minute
                    .addLimit(Bandwidth.builder()
                            .capacity(1000)
                            .refillIntervallyAligned(1000,
                                    Duration.ofMinutes(1),
                                    Instant.now())
                            .build())
                    .build();

            case "ANALYST" -> Bucket.builder()

                    // Secondary Burst limit for per Second
                    .addLimit(Bandwidth.builder()
                            .capacity(15)
                            .refillIntervallyAligned(15,
                                    Duration.ofSeconds(1),
                                    Instant.now())
                            .build())

                    // Primary Sustained limit for per Minute
                    .addLimit(Bandwidth.builder()
                            .capacity(500)
                            .refillIntervallyAligned(500,
                                    Duration.ofMinutes(1),
                                    Instant.now())
                            .build())
                    .build();

            default -> Bucket.builder()

                    // Secondary Burst limit for per Second
                    .addLimit(Bandwidth.builder()
                            .capacity(10)
                            .refillIntervallyAligned(10,
                                    Duration.ofSeconds(1),
                                    Instant.now())
                            .build())

                    // Primary Sustained limit for per Minute
                    .addLimit(Bandwidth.builder()
                            .capacity(100)
                            .refillIntervallyAligned(100,
                                    Duration.ofMinutes(1),
                                    Instant.now())
                            .build())
                    .build();
        };
    }
}


    // Change from elapsed time to fixed time tokens fill
//    .refillIntervally(100, Duration.ofMinutes(1))  ❌
//            .refillIntervallyAligned(100,
//                                     Duration.ofMinutes(1),
//    Instant.now())



/*
* Advance Rate limiter ->
* For production we could add tiered limits:
* short-term burst protection (10/sec) and
* sustained limit (100/min) to handle both
* flash attacks and slow drip attacks.

private Bucket buildBucket(
            int burstLimit,
            int sustainedLimit) {

        // Burst protection: short-term limit
        Bandwidth burst = Bandwidth.builder()
                .capacity(burstLimit)
                .refillIntervallyAligned(
                        burstLimit,
                        Duration.ofSeconds(1),
                        Instant.now())
                .build();

        // Sustained limit: long-term limit
        Bandwidth sustained = Bandwidth.builder()
                .capacity(sustainedLimit)
                .refillIntervallyAligned(
                        sustainedLimit,
                        Duration.ofMinutes(1),
                        Instant.now())
                .build();

        return Bucket.builder()
                .addLimit(burst)
                .addLimit(sustained)
                .build();
    }
 */
