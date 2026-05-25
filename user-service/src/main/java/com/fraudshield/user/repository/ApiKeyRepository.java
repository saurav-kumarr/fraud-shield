package com.fraudshield.user.repository;

import com.fraudshield.user.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByApiKey(String apiKey);
    Optional<ApiKey> findByMerchantEmail(String email);

}
