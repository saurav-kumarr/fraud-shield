package com.fraudshield.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

//    @Value("${user-service.url:http://localhost:8084}")
//    private String userServiceUrl;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_PREFIX = "/api/v1/";
    private static final String USER_SERVICE_URL =
            "http://user-service";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only check API key for B2B endpoints
        if (!path.startsWith(API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            sendError(response, HttpStatus.UNAUTHORIZED,
                    "X-API-Key header is required");
            return;
        }

        try {
            boolean isValid = restTemplate.getForObject(
                    USER_SERVICE_URL +
                            "/api/merchant/validate?apiKey="
                            + apiKey,
                    Boolean.class);

            if (Boolean.TRUE.equals(isValid)) {
                log.info("API Key validated: {}",
                        apiKey.substring(0, 10) + "...");
                MutableHttpServletRequest mutableRequest =
                        new MutableHttpServletRequest(request);
//                mutableRequest.putHeader("X-API-Key", apiKey);
                mutableRequest.putHeader("X-Auth-Type",
                        "API_KEY");
                filterChain.doFilter(mutableRequest, response);
            } else {
                sendError(response, HttpStatus.UNAUTHORIZED,
                        "Invalid API key");
            }
        } catch (Exception e) {
            log.error("API key validation failed: {}",
                    e.getMessage());
            sendError(response, HttpStatus.UNAUTHORIZED,
                    "API key validation failed");
        }
    }

    private void sendError(HttpServletResponse response,
                           HttpStatus status,
                           String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"" + message + "\"," +
                        "\"status\": " + status.value() + "}"
        );
    }
}
