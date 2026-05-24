# Alert Service

## Overview
Notification system for fraud events. Consumes fraud verdicts and sends real-time alerts via WebSocket and email.

## Port
`8083`

## Database
- PostgreSQL (`alert_db`)

## Responsibilities
- Consume fraud events from Kafka
- Store alerts in database
- Send real-time WebSocket notifications
- Send email alerts for BLOCKED transactions

## REST Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/alerts/my-alerts` | User's alerts (role-based) |
| GET | `/api/alerts/user/{userId}` | Alerts by user ID |
| GET | `/api/alerts/status/{status}` | Alerts by fraud status |

## Role-Based Access
- **USER** → Own alerts only
- **ADMIN/ANALYST** → All alerts

## Kafka
**Consumes:** `fraud-events`

## WebSocket
- Endpoint: `ws://localhost:8083/ws`
- Topic: `/topic/fraud-alerts`
- Real-time push for BLOCKED transactions

## Email (Stub)
- Configured but uses console logging
- Production: integrate AWS SES or SendGrid

## Database Schema
```sql
CREATE TABLE alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(36),
    user_id VARCHAR(100),
    merchant_id VARCHAR(100),
    amount DECIMAL(15,2),
    fraud_status VARCHAR(20),
    alert_status VARCHAR(20),
    risk_score DECIMAL(5,2),
    created_at TIMESTAMP
);
```

## Health Check
- `GET http://localhost:8083/actuator/health`