# SAGA Pattern

## Problem - Distributed Transactions
```
Monolith:
→ Single database
→ Single transaction
→ ACID guaranteed
→ Rollback easy ✅

Microservices:
→ Each service has own database
→ Cannot do single transaction
→ If step 3 fails after steps 1,2
→ How to rollback steps 1,2? ❌
```

## SAGA Solution
```
Break transaction into steps
Each step has compensating action
If step fails → run compensating actions
for completed steps
```

## Two Types of SAGA
```
1. Choreography (our approach)
→ Services react to events
→ No central coordinator
→ Kafka events drive flow
→ Loosely coupled ✅

2. Orchestration
→ Central saga orchestrator
→ Tells each service what to do
→ More control
→ More coupling ❌
```

## Our SAGA Flow

### Forward Steps
```
Step 1: Transaction Service
→ Create transaction (PENDING)
→ Publish TransactionEvent to Kafka

Step 2: Fraud Detection Service
→ Consume TransactionEvent
→ Evaluate fraud rules
→ Publish FraudAlertEvent to Kafka

Step 3: Alert Service
→ Consume FraudAlertEvent
→ Save alert to PostgreSQL
→ Send WebSocket notification
→ Send email if BLOCKED

Step 4: Report Service
→ Consume FraudAlertEvent
→ Save fraud report
→ Save fraud pattern
```

### Compensating Steps (Failure Handling)
```
If Fraud Detection fails:
→ Transaction marked REVIEW_PENDING
→ Manual review queue

If Alert Service fails:
→ Retry 3 times
→ Dead letter queue
→ Admin notification

If Report Service fails:
→ Retry automatically
→ Kafka offset not committed
→ Message reprocessed ✅
```

## Choreography Flow Diagram
```
Transaction Service
    ↓ publishes
Kafka [transaction-events]
    ↓ consumes
Fraud Detection Service
    ↓ publishes
Kafka [fraud-alerts]
    ↓ consumes (independently)
Alert Service     Report Service
    ↓                  ↓
Save Alert        Save Report
Send Notification Save Pattern
```

## Why Choreography over Orchestration
```
Choreography ✅:
→ Services fully independent
→ No single point of failure
→ Natural fit with Kafka
→ Easy to add new consumer
→ Loosely coupled

Orchestration ❌:
→ Orchestrator = single point of failure
→ Tight coupling
→ Complex orchestrator logic
→ Harder to maintain
```

## Idempotency
```
What if message consumed twice?

Kafka guarantees at-least-once delivery
→ Same message may arrive twice

Our protection:
→ Check transactionId before saving
→ Skip if already processed
→ Idempotent operations ✅
```

## Interview Answer

> "We implement the SAGA pattern using
> choreography-based approach with Kafka as
> the event bus. Each service publishes events
> and reacts to events independently — no central
> coordinator. Transaction Service publishes
> TransactionEvent, Fraud Detection Service
> consumes it and publishes FraudAlertEvent,
> which is then independently consumed by both
> Alert Service and Report Service. Failure
> handling uses Kafka's offset management —
> if a service fails, the offset is not committed
> and the message is reprocessed on recovery.
> This gives us eventual consistency without
> distributed transactions."