# Report Service

## Overview
Analytics and reporting service. Consumes fraud events and provides historical fraud reports and statistics.

## Port
`8085`

## Database
- PostgreSQL (`report_db`) - structured reports
- MongoDB (`fraud_patterns`) - aggregated patterns

## Responsibilities
- Generate fraud reports
- Provide statistics (today, total)
- Filter reports by user, merchant, status

## REST Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reports/my-reports` | User's reports (role-based) |
| GET | `/api/reports/user/{userId}` | Reports by user |
| GET | `/api/reports/status/{status}` | Reports by status |
| GET | `/api/reports/merchant/{merchantId}` | Reports by merchant |
| GET | `/api/reports/stats/total-fraud` | Total fraud count |
| GET | `/api/reports/stats/today-fraud` | Today's fraud count |

## Role-Based Access
- **USER** → Own reports only
- **ADMIN/ANALYST** → All reports

## Kafka
**Consumes:** `fraud-events`

## Database Schema
```sql
CREATE TABLE fraud_reports (
    report_id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(36),
    user_id VARCHAR(100),
    merchant_id VARCHAR(100),
    amount DECIMAL(15,2),
    risk_score DECIMAL(5,2),
    fraud_status VARCHAR(20),
    rule_details TEXT,
    created_at TIMESTAMP
);
```

## Health Check
- `GET http://localhost:8085/actuator/health`