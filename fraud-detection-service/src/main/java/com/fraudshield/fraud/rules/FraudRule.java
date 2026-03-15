package com.fraudshield.fraud.rules;


import com.fraudshield.fraud.dto.TransactionEvent;

public interface FraudRule {

    RuleResult evaluate(TransactionEvent transaction);

    String getRuleName();

    int getPriority();

}
