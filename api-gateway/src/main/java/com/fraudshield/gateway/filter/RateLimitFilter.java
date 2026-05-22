package com.fraudshield.gateway.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private static final String[] EXCLUDED_URLS = {
            "/actuator",
            "/ws"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip rate limit for excluded URLs
        for (String url : EXCLUDED_URLS) {
            if (path.startsWith(url)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Get user info from headers (set by JWT filter)
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        // For anonymous requests use IP address
        String key = userId != null
                ? userId
                : request.getRemoteAddr();

        String role = userRole != null
                ? userRole
                : "ANONYMOUS";

        Bucket bucket = rateLimiterService
                .resolveBucket(key, role);
        ConsumptionProbe probe = bucket
                .tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe
                    .getNanosToWaitForRefill()
                    / 1_000_000_000;

            log.warn("Rate limit exceeded for: {} role: {}",
                    key, role);

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After",
                    String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too many requests\"," +
                            "\"message\": \"Rate limit exceeded. " +
                            "Try again in " + waitForRefill +
                            " seconds\"," +
                            "\"status\": 429}"
            );
        }

    }

}
