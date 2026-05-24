# Fraud Detection Service

## Overview
Core fraud analysis engine. Consumes transaction events from Kafka, applies multiple fraud rules, and publishes verdicts.

## Port
`8082`

## Database
- MongoDB (`fraud_db`) - stores fraud patterns
- Redis - rule state (velocity, location, blacklists)

## Architecture
Uses **Chain of Responsibility + Strategy** design patterns through Rules Engine.

## Fraud Rules

| Rule | Priority | Score | Description |
|------|----------|-------|-------------|
| BlacklistRule | 1 | 100 | User/merchant/IP blacklisted |
| VelocityRule | 2 | 80 | >5 transactions in 10 minutes |
| AmountRule | 3 | 70/40 | High amount thresholds |
| LocationRule | 4 | 90 | Location change detected |

## Engine Logic
- Rules execute in priority order
- **Short-circuit** when score >= BLOCK_THRESHOLD (70)
- Final score = **MAX** of all rule scores (not average)

## Verdict
- Score >= 70 → **BLOCKED**
- Score >= 40 → **FLAGGED**
- Score < 40 → **APPROVED**

## Kafka
**Consumes:** `transaction-events`
**Publishes:** `fraud-events`

**Fraud Event Schema:**
```json
{
    "transactionId": "txn-001",
    "userId": "user@gmail.com",
    "riskScore": 95.0,
    "fraudStatus": "BLOCKED",
    "ruleResults": [...],
    "timestamp": "2026-05-24T10:30:01"
}
```

## Redis Keys
- `blacklist:user:{userId}` - blacklisted users
- `blacklist:merchant:{merchantId}` - blacklisted merchants
- `blacklist:ip:{ipAddress}` - blacklisted IPs
- `velocity:{userId}` - transaction count (TTL 600s)
- `location:{userId}` - last known location (TTL 3600s)

## Configuration
```yaml
fraud:
  block-threshold: 70
  flag-threshold: 40
  velocity-threshold: 5
  velocity-window-seconds: 600
```

## Health Check
- `GET http://localhost:8082/actuator/health`