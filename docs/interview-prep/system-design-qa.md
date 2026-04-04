# System Design Interview Q&A

## Main Question: Design a Real-Time Fraud Detection System

### Opening Statement (30 seconds)
```
"I actually built a production-grade real-time
fraud detection system called Fraud Shield.
Let me walk you through the architecture."
```

---

## Architecture Explanation (2 minutes)
```
"The system has 8 microservices:

1. API Gateway (8080)
   → Single entry point
   → JWT validation
   → Request routing

2. User Service (8084)
   → Registration and login
   → JWT token generation
   → Spring Security + BCrypt

3. Transaction Service (8081)
   → Receives all transactions
   → Saves to PostgreSQL
   → Publishes to Kafka

4. Fraud Detection Service (8082)
   → Consumes from Kafka
   → Runs Rules Engine
   → 4 fraud rules
   → Publishes result

5. Alert Service (8083)
   → Sends real-time WebSocket alerts
   → Email notifications
   → Saves to PostgreSQL

6. Report Service (8085)
   → Generates fraud reports
   → PostgreSQL + MongoDB

7. Eureka Server (8761)
   → Service discovery
   → Load balancing

8. Config Server (8888)
   → Centralized configuration
   → Dynamic refresh via Kafka"
```

---

## Deep Dive Questions

### Q: How does fraud detection work?
```
"I implemented a Rules Engine using
Chain of Responsibility + Strategy patterns.

4 rules execute in priority order:

1. BlacklistRule (priority 1)
   → Checks Redis for blacklisted
     users, merchants, IPs
   → Score: 100 if matched
   → Short-circuits immediately

2. VelocityRule (priority 2)
   → Redis INCR counter per user
   → TTL: 10 minutes window
   → Score: 80 if > 5 transactions

3. AmountRule (priority 3)
   → Checks transaction amount
   → Score: 70 if > 100,000
   → Score: 40 if > 50,000

4. LocationRule (priority 4)
   → Redis stores last location
   → TTL: 1 hour window
   → Score: 90 if location changed

Max score determines verdict:
→ Score >= 70 → BLOCKED
→ Score >= 40 → FLAGGED
→ Score < 40  → APPROVED"
```

---

### Q: How does the system handle 1 million transactions/second?
```
"Three levels of scalability:

1. Kafka Partitioning
   → transaction-events: 3 partitions
   → 3 fraud detection consumers
   → Process in parallel
   → Add partitions = linear scale

2. Redis Sub-millisecond
   → All fraud rule lookups from Redis
   → 0.1ms per check
   → No database bottleneck
   → Redis cluster for production

3. Horizontal Service Scaling
   → Multiple transaction-service instances
   → Eureka load balances
   → Stateless JWT auth
   → No sticky sessions needed"
```

---

### Q: What if Fraud Detection Service goes down?
```
"Kafka handles this perfectly:

1. Transactions continue processing
   → transaction-service unaffected
   → Messages queue in Kafka

2. Messages wait in Kafka
   → Retention: 7 days default
   → No messages lost

3. Service recovers
   → Consumes queued messages
   → Processes in order
   → offset committed per message

This is exactly why we chose
async Kafka over sync REST calls."
```

---

### Q: How do you prevent false positives?
```
"Multi-layer scoring system:

1. Each rule assigns risk score
   → Not binary block/allow
   → Graduated scoring

2. Max score determines verdict
   → 70+ = BLOCKED
   → 40-69 = FLAGGED
   → 0-39 = APPROVED

3. FLAGGED = manual review
   → Human reviews borderline cases
   → Not auto-blocked

4. Configurable thresholds
   → Config Server
   → Change without restart
   → Tune per business needs"
```

---

### Q: How do you update fraud rules without downtime?
```
"Two levels of zero-downtime updates:

1. Config changes (thresholds):
   → Update Config Server yml
   → POST /actuator/busrefresh
   → All services refresh via Kafka
   → Zero restart needed ✅

2. New fraud rule:
   → Create new class implementing FraudRule
   → Deploy new jar
   → Rolling deployment
   → Eureka handles traffic shift ✅"
```

---

### Q: How is the system secured?
```
"Multiple security layers:

1. JWT at API Gateway
   → All requests validated
   → Invalid tokens rejected
   → Never reach backend

2. BCrypt passwords
   → Salted hashing
   → Cannot be reversed

3. Secrets management
   → Environment variables locally
   → AWS Secrets Manager in production
   → Never in code or Git

4. CORS configuration
   → Restricted to known origins
   → Prevents CSRF

5. Redis blacklist
   → Known fraudulent entities
   → Instant block ✅"
```

---

### Q: How do services communicate?
```
"Two patterns:

1. Synchronous (REST via Gateway)
   → Client → Gateway → Service
   → Used for: user requests

2. Asynchronous (Kafka events)
   → Service → Kafka → Service
   → Used for: inter-service communication
   → Decoupled, fault tolerant

Never direct service-to-service REST
→ Tight coupling ❌
→ Kafka for everything internal ✅"
```

---

### Q: What databases do you use and why?
```
"Polyglot persistence:

PostgreSQL:
→ transactions, users, alerts, reports
→ ACID compliance
→ Financial data integrity

MongoDB:
→ fraud_patterns
→ Flexible schema
→ Different patterns = different fields

Redis:
→ Velocity counters
→ Location cache
→ Blacklist cache
→ Sub-millisecond access

Each database chosen for its strength ✅"
```

---

## Closing Statement
```
"This system mirrors production architecture
at companies like Razorpay and PhonePe.
The same tech stack — Spring Boot, Kafka,
Redis — processes billions of transactions
in real-time at these companies.

Key differentiators:
→ Rules Engine with short-circuit logic
→ Dynamic config refresh (zero downtime)
→ Polyglot persistence
→ Event-driven SAGA pattern
→ JWT at gateway level

I can discuss any component in detail."
```