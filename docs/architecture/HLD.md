# High Level Design (HLD) - Fraud Shield

## System Overview

Fraud Shield is a real-time fraud detection system that processes
financial transactions, detects fraudulent patterns using a
rules engine, and sends real-time alerts.

---

## Architecture Diagram
```
                        ┌─────────────────┐
                        │     Client      │
                        │  (React/Postman)│
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   API Gateway   │
                        │   Port: 8080    │
                        │ JWT Validation  │
                        │ Rate Limiting   │
                        │ CORS Config     │
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────▼──────┐    ┌──────────▼─────┐    ┌──────────▼─────┐
│  User Service  │    │  Transaction   │    │ Report Service │
│  Port: 8084    │    │    Service     │    │  Port: 8085    │
│  JWT Auth      │    │  Port: 8081    │    │  Fraud Reports │
│  PostgreSQL    │    │  PostgreSQL    │    │  PostgreSQL    │
└────────────────┘    │  Redis         │    │  MongoDB       │
                      └──────┬─────────┘    └────────────────┘
                             │
                      ┌──────▼─────────┐
                      │  Apache Kafka  │
                      │ transaction-   │
                      │    events      │
                      └──────┬─────────┘
                             │
                      ┌──────▼─────────┐
                      │    Fraud       │
                      │  Detection     │
                      │   Service      │
                      │  Port: 8082    │
                      │  Rules Engine  │
                      │  Redis+MongoDB │
                      └──────┬─────────┘
                             │
                      ┌──────▼─────────┐
                      │  Apache Kafka  │
                      │  fraud-alerts  │
                      └──────┬─────────┘
                             │
                      ┌──────▼─────────┐
                      │ Alert Service  │
                      │  Port: 8083    │
                      │  WebSocket     │
                      │  Email Alerts  │
                      │  PostgreSQL    │
                      └────────────────┘
```

---

## Services Overview

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| api-gateway | 8080 | - | Single entry point, JWT validation |
| user-service | 8084 | PostgreSQL | Authentication, JWT generation |
| transaction-service | 8081 | PostgreSQL, Redis | Transaction processing |
| fraud-detection-service | 8082 | MongoDB, Redis | Fraud detection rules engine |
| alert-service | 8083 | PostgreSQL | Notifications, WebSocket |
| report-service | 8085 | PostgreSQL, MongoDB | Analytics, Reports |
| eureka-server | 8761 | - | Service discovery |
| config-server | 8888 | - | Centralized configuration |

---

## Infrastructure

| Component | Purpose | Port |
|-----------|---------|------|
| Apache Kafka | Event streaming | 9092 |
| Redis | Caching, Rate limiting | 6379 |
| PostgreSQL | Relational data | 5433 |
| MongoDB | Fraud patterns | 27017 |
| Prometheus | Metrics collection | 9090 |
| Grafana | Monitoring dashboard | 3000 |
| Zookeeper | Kafka coordination | 2181 |

---

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|---------|---------|---------|
| transaction-events | transaction-service | fraud-detection-service | New transactions |
| fraud-alerts | fraud-detection-service | alert-service, report-service | Fraud results |
| notification-events | - | - | Future notifications |
| audit-log | - | - | Audit trail |
| springCloudBus | config-server | all services | Config refresh |

---

## Transaction Flow
```
Step 1: Client sends transaction to API Gateway
Step 2: Gateway validates JWT token
Step 3: Gateway routes to Transaction Service
Step 4: Transaction saved to PostgreSQL (PENDING)
Step 5: TransactionEvent published to Kafka
Step 6: Fraud Detection Service consumes event
Step 7: Rules Engine evaluates transaction
Step 8: FraudAlertEvent published to Kafka
Step 9: Alert Service consumes → saves + WebSocket
Step 10: Report Service consumes → saves report
Step 11: Transaction status updated
```

---

## Scalability
```
Horizontal Scaling:
→ Each service scales independently
→ Kafka partitions enable parallel processing
→ Redis cluster for distributed caching
→ Multiple service instances behind gateway

Vertical Scaling:
→ Increase resources per service
→ Kafka broker scaling
→ Database read replicas
```

---

## Security
```
→ JWT authentication at gateway
→ BCrypt password hashing
→ Secrets in environment variables
→ CORS configured
→ No sensitive data in logs
```