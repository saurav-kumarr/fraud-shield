# Low Level Design (LLD) - Fraud Shield

## Transaction Service

### Classes
```
TransactionController
→ POST /api/transactions
→ GET /api/transactions/{id}
→ GET /api/transactions/user/{userId}

TransactionService
→ createTransaction()
→ getTransactionById()
→ getTransactionsByUserId()

TransactionRepository
→ findByUserId()
→ findByStatus()
→ countByUserIdAndCreatedAtAfter()

TransactionProducer
→ publishTransaction()

KafkaConfig
→ transactionEventsTopic()
→ fraudAlertsTopic()
```

### Database Schema (PostgreSQL)
```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    amount NUMERIC(38,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    type VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Transaction Status Flow
```
PENDING → APPROVED ✅
PENDING → BLOCKED ❌
PENDING → FLAGGED ⚠️
FLAGGED → REVIEW_PENDING
```

---

## Fraud Detection Service

### Rules Engine Design
```
FraudRule (Interface)
→ evaluate(TransactionEvent) → RuleResult
→ getRuleName() → String
→ getPriority() → int

Implementations:
BlacklistRule  (priority 1) → checks Redis blacklist
VelocityRule   (priority 2) → checks Redis counter
AmountRule     (priority 3) → checks amount thresholds
LocationRule   (priority 4) → checks Redis location

FraudDetectionEngine
→ Sorts rules by priority
→ Short-circuits on high risk score
→ Returns max score as verdict
```

### Scoring Logic
```
Score >= 70 → BLOCKED
Score >= 40 → FLAGGED
Score < 40  → APPROVED

BlacklistRule → 100 (immediate block)
LocationRule  → 90
VelocityRule  → 80
AmountRule    → 70 (high) / 40 (medium)
```

### Redis Keys
```
velocity:{userId}     → transaction count (TTL: 600s)
location:{userId}     → last location (TTL: 3600s)
blacklist:user:{id}   → blacklisted user
blacklist:merchant:{id} → blacklisted merchant
blacklist:ip:{ip}     → blacklisted IP
```

---

## User Service

### Classes
```
AuthController
→ POST /api/auth/register
→ POST /api/auth/login

UserService
→ register()
→ login()

JwtService
→ generateToken()
→ isTokenValid()
→ extractUsername()

JwtAuthFilter
→ doFilterInternal()

SecurityConfig
→ securityFilterChain()
→ authenticationProvider()
→ passwordEncoder()

UserDetailsServiceImpl
→ loadUserByUsername()
```

### Database Schema (PostgreSQL)
```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    enabled BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### JWT Structure
```
Header:
{
  "alg": "HS384"
}

Payload:
{
  "sub": "user@email.com",
  "iat": 1234567890,
  "exp": 1234654290
}

Signature: HMAC-SHA384 signed ✅
```

---

## Alert Service

### Classes
```
FraudAlertConsumer
→ consumeFraudAlert()

AlertService
→ processAlert()
→ saveAlert()
→ sendWebSocketAlert()
→ sendEmailAlert()

WebSocketConfig
→ configureMessageBroker()
→ registerStompEndpoints()

WebSocketController
→ handleAlert()
```

### WebSocket Flow
```
Client connects to: ws://localhost:8083/ws
Client subscribes to: /topic/fraud-alerts
Server pushes: FraudAlertEvent JSON
Client receives real-time alert ✅
```

### Database Schema (PostgreSQL)
```sql
CREATE TABLE alerts (
    id BIGINT PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    amount NUMERIC(38,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    fraud_status VARCHAR(255) NOT NULL,
    risk_score DOUBLE NOT NULL,
    risk_reason VARCHAR(1000),
    alert_status VARCHAR(255),
    created_at TIMESTAMP
);
```

---

## Report Service

### Classes
```
FraudAlertConsumer
→ consumeFraudAlert()

ReportService
→ generateReport()
→ saveFraudReport()
→ saveFraudPattern()
→ determinePatternType()
→ getReportsByUserId()
→ getTotalFraudCount()
→ getTodayFraudCount()

ReportController
→ GET /api/reports/user/{userId}
→ GET /api/reports/status/{status}
→ GET /api/reports/merchant/{merchantId}
→ GET /api/reports/stats/total-fraud
→ GET /api/reports/stats/today-fraud
```

### Pattern Types
```
VELOCITY    → Too many transactions
HIGH_AMOUNT → Amount exceeds threshold
LOCATION    → Geographic anomaly
BLACKLIST   → Known fraudulent entity
UNKNOWN     → Unclassified pattern
```

---

## API Gateway

### Routing Table
```
/api/auth/**         → user-service:8084
/api/users/**        → user-service:8084
/api/transactions/** → transaction-service:8081
/api/fraud/**        → fraud-detection-service:8082
/api/alerts/**       → alert-service:8083
/api/reports/**      → report-service:8085
```

### Filter Chain
```
Request
  ↓
CorsFilter
  ↓
JwtAuthenticationFilter
  ↓ (if valid)
Gateway Routing
  ↓
Backend Service
```