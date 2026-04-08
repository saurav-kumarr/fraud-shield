package com.fraudshield.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;


@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.jwt.secret}")
    private String secretKey;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/register",
            "/api/auth/login",
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

        // Skip public URLs
        for (String url : PUBLIC_URLS) {
            if (path.startsWith(url)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response,
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header");
            return;
        }
        
        try {
            String token = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            claims.getSubject(), null, List.of());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Add user info to headers for downstream services
            HttpServletRequestWrapper modifiedRequest =
                    new HttpServletRequestWrapper(request) {
                        @Override
                        public String getHeader(String name) {
                            if ("X-User-Id".equals(name))
                                return claims.getSubject();
                            if ("X-User-Email".equals(name))
                                return claims.getSubject();
                            if ("X-User-Role".equals(name)) {
                                Object role = claims.get("role");
                                return role != null ?
                                        role.toString() : "USER";
                            }
                            return super.getHeader(name);
                        }
                    };

            log.info("JWT validated for user: {}", claims.getSubject());
            filterChain.doFilter(modifiedRequest, response);
        } catch (Exception e) {

            log.error("JWT validation failed: {}", e.getMessage());
            sendError(response,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token");
        }

    }

    private void sendError(HttpServletResponse response,
                           HttpStatus status,
                           String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"" + message + "\"," +
                        "\"status\": " + status.value() + "}"
        );
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }
}
