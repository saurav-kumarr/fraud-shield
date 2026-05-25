package com.fraudshield.user.controller;

import com.fraudshield.user.dto.ApiKeyResponse;
import com.fraudshield.user.dto.MerchantRegistrationRequest;
import com.fraudshield.user.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/register")
    public ResponseEntity<ApiKeyResponse> registerMerchant(
            @Valid @RequestBody MerchantRegistrationRequest request) {
        log.info("Merchant registration: {}",
                request.getCompanyName());
        ApiKeyResponse response = apiKeyService
                .generateApiKey(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateApiKey(
            @RequestParam String apiKey) {
        boolean isValid = apiKeyService.validateApiKey(apiKey);
        return ResponseEntity.ok(isValid);
    }

}