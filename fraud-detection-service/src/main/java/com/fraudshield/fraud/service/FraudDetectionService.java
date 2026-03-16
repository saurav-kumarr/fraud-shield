package com.fraudshield.fraud.service;

import com.fraudshield.fraud.dto.TransactionEvent;
import com.fraudshield.fraud.engine.EngineResult;
import com.fraudshield.fraud.engine.FraudDetectionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudDetectionEngine fraudDetectionEngine;

    public EngineResult analyzeTransaction(TransactionEvent transaction) {
        log.info("Analyzing transaction: {} for userId: {}",
                transaction.getTransactionId(),
                transaction.getUserId());

        EngineResult result = fraudDetectionEngine.analyze(transaction);

        log.info("Analysis result for transactionId: {} " +
                        "verdict: {} score: {} reason: {}",
                transaction.getTransactionId(),
                result.getVerdict(),
                result.getRiskScore(),
                result.getReason());

        return result;
    }
}
