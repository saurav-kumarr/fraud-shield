# Apache Kafka

## What is Kafka?
```
Apache Kafka is a distributed event streaming platform.
Stores data in RAM instead of disk for extreme speed.

Think of it as:
→ A highly scalable message queue
→ Can handle millions of events per second
→ Messages retained even after consumption
→ Multiple consumers can read same message
```

## Core Concepts

### Topic
```
Category of messages (like a table in database)

Our topics:
→ transaction-events
→ fraud-alerts
→ notification-events
→ audit-log
→ springCloudBus
```

### Partition
```
Parallel lanes within a topic

Without partitions (1 lane):
→ One message at a time ❌
→ Slow processing

With partitions (3 lanes):
→ 3 messages simultaneously ✅
→ 3x throughput

Our config:
→ transaction-events: 3 partitions
→ fraud-alerts: 3 partitions
```

### Producer
```
Service that SENDS messages to Kafka

Our producers:
→ transaction-service → transaction-events
→ fraud-detection-service → fraud-alerts
```

### Consumer
```
Service that READS messages from Kafka

Our consumers:
→ fraud-detection-service ← transaction-events
→ alert-service ← fraud-alerts
→ report-service ← fraud-alerts
```

### Consumer Group
```
Multiple consumers sharing work

fraud-detection-group:
→ All instances share transaction-events
→ Each message processed ONCE ✅

alert-group:
→ All instances share fraud-alerts
→ Each message processed ONCE ✅

Different groups:
→ alert-group AND report-group
→ BOTH receive same fraud-alert
→ Pub/Sub pattern ✅
```

### Offset
```
Position of consumer in topic

Kafka tracks:
→ Last message read per consumer group
→ Resumes from last offset on restart

auto-offset-reset: earliest
→ Start from beginning if no offset ✅
```

## Kafka in Our Project

### Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
    consumer:
      group-id: fraud-detection-group
      auto-offset-reset: earliest
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
      properties:
        spring.json.use.type.headers: false
        spring.json.value.default.type:
          com.fraudshield.fraud.dto.TransactionEvent
```

### Type Mapping Fix
```
Problem:
→ Producer serializes with class header
→ Consumer can't find class ❌

Solution:
spring.json.use.type.headers: false
spring.json.value.default.type: OurClass
→ Ignore header, use our class ✅
```

### KafkaListener
```java
@KafkaListener(
    topics = "${kafka.topic.transaction-events}",
    groupId = "${spring.kafka.consumer.group-id}"
)
public void consumeTransactionEvent(
        TransactionEvent event) {
    // process event
}
```

### KafkaTemplate
```java
kafkaTemplate.send(topic, key, value)
    .whenComplete((result, ex) -> {
        if (ex == null) {
            // success - log partition and offset
        } else {
            // failure - log error
        }
    });
```

## Consumer Tuning
```yaml
properties:
  session.timeout.ms: 30000
  heartbeat.interval.ms: 10000

Rule: heartbeat < session.timeout/3
10000 < 30000/3 = 10000 ✅
```

## Interview Questions

**Q: What is Kafka and why use it?**
```
Kafka is distributed event streaming platform.
We use it for decoupling microservices,
enabling async processing, fault tolerance
and replay capability.
```

**Q: What is consumer group?**
```
Multiple consumers sharing work.
Each partition assigned to one consumer.
Enables horizontal scaling.
Different groups receive same message.
```

**Q: How does Kafka ensure no message loss?**
```
→ Replication factor (replicas: 3 in production)
→ Acknowledgment from all replicas
→ Consumer commits offset after processing
→ Failed consumer = message reprocessed
```

**Q: What is offset?**
```
Position in partition.
Consumer tracks last read position.
Enables exactly-once or at-least-once delivery.
```

**Q: Kafka vs RabbitMQ?**
```
Kafka:
→ Message retention
→ Replay capability
→ Higher throughput
→ Event streaming

RabbitMQ:
→ Message deleted after consumption
→ Better for task queues
→ More routing options
```