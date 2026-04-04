# Kafka Interview Questions

## Basic Questions

### Q1: What is Apache Kafka?
```
Kafka is a distributed event streaming platform.
Designed for high throughput, fault tolerance
and real-time data pipelines.

Key features:
→ Publish/Subscribe messaging
→ Message retention
→ Horizontal scalability
→ Fault tolerance via replication
→ Replay capability
```

### Q2: What is a Topic?
```
Category of messages in Kafka.
Like a table in database.

Our topics:
→ transaction-events
→ fraud-alerts
→ notification-events
→ audit-log
→ springCloudBus
```

### Q3: What is a Partition?
```
Parallel lane within a topic.

Benefits:
→ Parallel processing
→ Higher throughput
→ Multiple consumers

Our config:
→ transaction-events: 3 partitions
→ fraud-alerts: 3 partitions
→ 3x throughput ✅
```

### Q4: What is a Consumer Group?
```
Multiple consumers sharing work.

Same group:
→ Each partition → one consumer
→ Each message processed ONCE
→ Horizontal scaling ✅

Different groups:
→ Both receive same message
→ Pub/Sub pattern ✅

Our groups:
→ fraud-detection-group
→ alert-group
→ report-group
```

### Q5: What is an Offset?
```
Position of consumer in partition.

Kafka tracks per consumer group.
Consumer commits after processing.
Enables replay from any point.

auto-offset-reset: earliest
→ Start from beginning if no offset
```

---

## Intermediate Questions

### Q6: How does Kafka ensure fault tolerance?
```
Replication:
→ Each partition has replicas
→ Leader + followers
→ If leader fails → follower becomes leader
→ No data loss

Our dev config: replicas(1)
Production: replicas(3) ✅
```

### Q7: What is the difference between at-least-once and exactly-once delivery?
```
At-least-once (our setup):
→ Message may be delivered twice
→ Consumer must be idempotent
→ Check if already processed

Exactly-once:
→ Kafka transactions
→ Higher overhead
→ Use for financial systems

Our mitigation:
→ Check transactionId before saving
→ Skip duplicates ✅
```

### Q8: Why did you choose Kafka over REST?
```
REST (synchronous):
❌ Tight coupling
❌ If fraud service down → transaction fails
❌ Cannot replay
❌ Cannot have multiple consumers

Kafka (asynchronous):
✅ Loose coupling
✅ Fault tolerant
✅ Replay capability
✅ Multiple consumer groups
✅ Transaction processing unaffected
```

### Q9: What is Spring Cloud Bus and how does it use Kafka?
```
Spring Cloud Bus uses Kafka as
message broker for config refresh.

Flow:
1. POST /actuator/busrefresh
2. Config Server publishes to springCloudBus
3. ALL services receive RefreshEvent
4. Each service fetches new config
5. Zero downtime config update ✅
```

### Q10: How do you handle deserialization errors?
```
Problem:
→ Producer publishes class A
→ Consumer expects class B
→ ClassNotFoundException ❌

Solution in our project:
spring.json.use.type.headers: false
spring.json.value.default.type: OurClass

→ Ignore type header
→ Always deserialize to OurClass ✅
```

---

## Advanced Questions

### Q11: How does Kafka handle backpressure?
```
Consumer processes slower than producer:
→ Messages queue in partition
→ Consumer catches up at own pace
→ No data loss ✅

max.poll.records: 500
→ Process 500 messages per poll
→ Tune for performance
```

### Q12: What are Kafka consumer properties we tuned?
```
session.timeout.ms: 30000
→ Consumer considered dead after 30s
→ Triggers rebalance

heartbeat.interval.ms: 10000
→ Consumer sends heartbeat every 10s
→ Must be < session.timeout/3

auto.offset.reset: earliest
→ Start from beginning if no offset
→ Ensures no messages missed
```

### Q13: What is a Kafka Rebalance?
```
Triggered when:
→ Consumer joins group
→ Consumer leaves group
→ Consumer crashes (session timeout)

During rebalance:
→ Partitions reassigned
→ Brief pause in consumption
→ Then resumes ✅
```

### Q14: How do you monitor Kafka?
```
Our setup:
→ Prometheus collects metrics
→ Grafana visualizes

Key metrics:
→ Consumer lag (messages behind)
→ Throughput per topic
→ Error rate
→ Partition distribution
```

### Q15: What is idempotent producer?
```
enable.idempotence: true
→ Exactly-once semantics
→ No duplicate messages
→ Sequence numbers per partition
→ Broker deduplicates ✅
```

---

## Project Specific Questions

### Q16: How many topics do you have?
```
5 topics:
→ transaction-events (3 partitions)
→ fraud-alerts (3 partitions)
→ notification-events (1 partition)
→ audit-log (1 partition)
→ springCloudBus (1 partition)
```

### Q17: Who creates the topics?
```
transaction-service creates ALL topics
via KafkaConfig @Bean definitions.

TopicBuilder.name("topic")
    .partitions(3)
    .replicas(1)
    .build();

Runs on startup.
Skips if already exists. ✅
```

### Q18: How do you test Kafka locally?
```
Docker Compose:
→ Zookeeper container
→ Kafka container

docker-compose up -d
→ All infrastructure ready
→ No local installation needed ✅
```

### Q19: What is Zookeeper's role?
```
Kafka cannot run alone.
Zookeeper manages Kafka brokers:
→ Leader election
→ Broker registration
→ Topic metadata

KRaft mode (future):
→ Kafka without Zookeeper
→ Available in newer versions
```

### Q20: How would you scale Kafka for production?
```
1. Increase partitions
   → More parallel consumers
   → Linear throughput scale

2. Increase replicas
   → replicas(3) for fault tolerance
   → No data loss on broker failure

3. Kafka cluster
   → Multiple brokers
   → Distribute partitions
   → Handle broker failures

4. Consumer scaling
   → Add more instances
   → Auto partition assignment
   → Eureka for service discovery
```