# Transaction Service

## Overview
Manages transaction lifecycle. Receives transaction requests, persists them, and publishes events to Kafka for fraud analysis.

## Port
`8081`

## Database
- PostgreSQL (`transaction_db`)
- Redis (caching)

## Responsibilities
- Create transactions
- Auto-fill userId from JWT (via UserContext)
- Publish TransactionEvent to Kafka
- Provide transaction history APIs

## REST Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/transactions` | Create new transaction |
| GET | `/api/transactions/my-transactions` | Get logged user transactions |
| GET | `/api/transactions/{id}` | Get transaction by ID |
| GET | `/api/transactions/user/{userId}` | Get transactions by user |

## Kafka
**Publishes to:** `transaction-events`

**Event Schema:**
```json
{
    "transactionId": "txn-001",
    "userId": "user@gmail.com",
    "merchantId": "merchant-001",
    "amount": 5000.00,
    "currency": "INR",
    "deviceId": "device-001",
    "ipAddress": "192.168.1.1",
    "location": "Delhi, India",
    "type": "PAYMENT",
    "timestamp": "2026-05-24T10:30:00"
}
```

## Database Schema
**Table:** `transactions`
```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    merchant_id VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3),
    device_id VARCHAR(100),
    ip_address VARCHAR(50),
    location VARCHAR(200),
    type VARCHAR(20),
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Security
- No Spring Security (Gateway handles auth)
- Reads user info from `X-User-Id`, `X-User-Role` headers
- UserContext bean provides easy access

## Exception Handling
- `ResourceNotFoundException` → 404
- `BadRequestException` → 400
- Validation errors → 400 with field details

## Health Check
- `GET http://localhost:8081/actuator/health`