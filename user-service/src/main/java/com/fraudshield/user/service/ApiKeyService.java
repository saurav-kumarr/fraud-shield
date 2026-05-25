package com.fraudshield.user.service;

import com.fraudshield.user.dto.MerchantRegistrationRequest;
import com.fraudshield.user.dto.ApiKeyResponse;
import com.fraudshield.user.exception.BadRequestException;
import com.fraudshield.user.model.ApiKey;
import com.fraudshield.user.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyResponse generateApiKey(
            MerchantRegistrationRequest request) {

        log.info("Generating API key for merchant: {}",
                request.getCompanyName());

        if (apiKeyRepository.findByMerchantEmail(
                request.getMerchantEmail()).isPresent()) {
            throw new BadRequestException(
                    "Merchant already registered: "
                            + request.getMerchantEmail());
        }

        String apiKey = "fsk_" + UUID.randomUUID()
                .toString().replace("-", "");

        ApiKey newKey = ApiKey.builder()
                .apiKey(apiKey)
                .merchantName(request.getMerchantName())
                .merchantEmail(request.getMerchantEmail())
                .companyName(request.getCompanyName())
                .webhookUrl(request.getWebhookUrl())
                .build();

        apiKeyRepository.save(newKey);

        log.info("API key generated for: {}",
                request.getCompanyName());

        return ApiKeyResponse.builder()
                .apiKey(apiKey)
                .merchantName(request.getMerchantName())
                .companyName(request.getCompanyName())
                .tier(newKey.getTier().name())
                .expiresAt(newKey.getExpiresAt())
                .message("Save this API key securely. " +
                        "It won't be shown again.")
                .build();
    }

    public boolean validateApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey)
                .map(key -> key.isActive() &&
                        key.getExpiresAt()
                                .isAfter(java.time.LocalDateTime.now()))
                .orElse(false);
    }

    public ApiKey getByApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid API key"));
    }
}
