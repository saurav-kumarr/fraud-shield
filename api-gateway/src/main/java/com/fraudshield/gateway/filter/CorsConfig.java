package com.fraudshield.gateway.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Required if browser sends credentials/cookies
        corsConfiguration.setAllowCredentials(true);

        // Frontend origins allowed to call API Gateway directly
        corsConfiguration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        corsConfiguration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        corsConfiguration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // Allow frontend JavaScript to read these response headers
        corsConfiguration.setExposedHeaders(List.of(
                "X-Rate-Limit-Remaining",
                "X-Rate-Limit-Retry-After"
        ));


        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(
                        new CorsFilter(source));

        // CRITICAL: Set highest priority ✅
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
