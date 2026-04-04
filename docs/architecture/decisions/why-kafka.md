# Why Apache Kafka?

## Problem Without Kafka
```
Direct REST call approach:
Transaction Service → calls → Fraud Detection Service

Problems:
❌ Tight coupling between services
❌ If fraud service is slow → transaction waits
❌ If fraud service is down → transaction fails
❌ Cannot replay failed events
❌ Cannot scale consumers independently
```

## Why We Chose Kafka

### 1. Decoupling
```
Transaction Service publishes event
→ Returns immediately ✅
→ Doesn't wait for fraud detection

Fraud Detection consumes independently
→ Processes at its own pace ✅
→ No dependency on transaction service
```

### 2. Fault Tolerance
```
Fraud Detection Service down:
→ Messages wait in Kafka ✅
→ No transaction lost
→ When service recovers → processes queue

Without Kafka:
→ Messages lost forever ❌
```

### 3. Replay Capability
```
New fraud rule added:
→ Reprocess historical transactions
→ Kafka retains messages
→ Set offset to beginning → replay ✅

Cannot do this with REST calls ❌
```

### 4. Multiple Consumers
```
fraud-alerts topic consumed by:
→ alert-service  → sends notifications
→ report-service → generates reports

Same message → multiple consumers
Pub/Sub pattern ✅
```

### 5. Scalability
```
3 partitions = 3 parallel consumers
→ 3x throughput

Add more partitions:
→ Linear scale up ✅

Razorpay processes 5 billion
transactions using Kafka ✅
```

## Kafka vs RabbitMQ

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| Message retention | Yes ✅ | No ❌ |
| Replay | Yes ✅ | No ❌ |
| Throughput | Very high ✅ | Medium |
| Multiple consumers | Yes ✅ | Complex |
| Use case | Event streaming ✅ | Task queue |

## Our Kafka Topics
```
transaction-events (3 partitions)
→ Producer: transaction-service
→ Consumer: fraud-detection-service

fraud-alerts (3 partitions)
→ Producer: fraud-detection-service
→ Consumer: alert-service, report-service

springCloudBus (1 partition)
→ Used for config refresh
```

## Interview Answer

> "We chose Kafka because transaction processing
> and fraud detection are naturally asynchronous.
> Using Kafka completely decouples these services.
> If fraud detection is slow or down, transactions
> continue processing without impact. Kafka also
> gives us message replay capability — if fraud rules
> change, we can reprocess historical transactions.
> The pub/sub pattern allows both alert-service and
> report-service to independently consume fraud alerts
> without any coordination. This mirrors how companies
> like Razorpay and PhonePe handle billions of
> transactions in real-time."