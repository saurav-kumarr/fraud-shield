package com.fraudshield.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private String apiKey;
    private String merchantName;
    private String companyName;
    private String tier;
    private LocalDateTime expiresAt;
    private String message;
}