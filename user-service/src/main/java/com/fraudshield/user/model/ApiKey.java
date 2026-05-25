package com.fraudshield.user.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", unique = true,
            nullable = false, length = 100)
    private String apiKey;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "merchant_email", nullable = false)
    private String merchantEmail;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier")
    private ApiKeyTier tier;

    @Column(name = "active")
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusYears(1);
        if (tier == null) tier = ApiKeyTier.BASIC;
        active = true;
    }
}