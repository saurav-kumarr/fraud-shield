package com.fraudshield.report.repository;

import com.fraudshield.report.model.FraudPattern;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudPatternRepository extends MongoRepository<FraudPattern, String> {

    List<FraudPattern> findByUserId(String userId);

    List<FraudPattern> findByPatternType(String patternType);

    List<FraudPattern> findByFraudStatus(String fraudStatus);

}
