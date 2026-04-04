# Why Microservices Architecture?

## Problem with Monolith
```
Single monolithic application:
→ Transaction processing
→ Fraud detection
→ Alert sending
→ Report generation
→ User management

Problems:
❌ Scale entire app for one bottleneck
❌ Single point of failure
❌ One team blocks others
❌ Cannot use best database per need
❌ Deploy entire app for small change
```

## Why We Chose Microservices

### 1. Independent Scaling
```
Fraud Detection needs more CPU:
→ Scale only fraud-detection-service
→ Other services unchanged
→ Cost efficient on AWS ✅

Without microservices:
→ Scale entire application
→ Wasteful and expensive ❌
```

### 2. Fault Isolation
```
Alert Service crashes:
→ Transaction processing continues ✅
→ Fraud detection continues ✅
→ Only alerts affected

Monolith crash:
→ Everything stops ❌
```

### 3. Technology Flexibility
```
Each service uses best database:
→ Transaction Service → PostgreSQL (ACID)
→ Fraud Detection   → MongoDB (flexible)
→ Rules Cache       → Redis (speed)
→ Pattern Storage   → MongoDB (schemaless)
```

### 4. Independent Deployment
```
Update fraud rules:
→ Deploy only fraud-detection-service
→ Zero downtime for other services
→ No risk to transaction processing ✅
```

## Alternative Considered
```
Monolithic Architecture:
→ Simpler initially
→ No network overhead
→ Easier debugging

Rejected because:
→ Cannot scale fraud detection independently
→ Single point of failure
→ Cannot use multiple databases
→ Team dependency issues
```

## Real World Reference
```
Razorpay → Microservices
PhonePe  → Microservices
Paytm    → Microservices

All major fintech companies use
microservices for payment processing ✅
```

## Interview Answer

> "We chose microservices because fraud detection
> is computationally intensive and needs independent
> scaling. Each service has its own database optimized
> for its use case — PostgreSQL for ACID transactions,
> MongoDB for flexible fraud patterns, Redis for
> sub-millisecond rule lookups. Fault isolation ensures
> a crash in alert service doesn't affect transaction
> processing. This mirrors exactly how companies like
> Razorpay and PhonePe architect their payment systems."