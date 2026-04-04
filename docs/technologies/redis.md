# Redis

## What is Redis?
```
Redis = Remote Dictionary Server

In-memory data structure store:
→ Stores data in RAM
→ 0.1ms response time
→ 100x faster than database

Supports multiple data structures:
→ String
→ Hash
→ List
→ Set
→ Sorted Set
```

## Why Redis in Fraud Detection?
```
Fraud detection requires:
→ Sub-millisecond rule lookups
→ Atomic counter operations
→ TTL based expiry
→ High throughput

Redis perfect for all these ✅
```

## Redis in Our Project

### 1. Velocity Rule
```
Key: velocity:user-001
Type: String (counter)
TTL: 600 seconds (10 minutes)

Operation:
INCR velocity:user-001
→ Atomic increment
→ Returns new count
→ Thread safe ✅

First transaction:
SET velocity:user-001 1
EXPIRE velocity:user-001 600

6th transaction in 10 min:
→ count > 5 → FRAUD! ❌
```

### 2. Location Tracking
```
Key: location:user-001
Type: String
TTL: 3600 seconds (1 hour)

SET location:user-001 "Delhi, India" EX 3600

Next transaction:
GET location:user-001
→ "Delhi, India"

Current location: "Mumbai, India"
→ Different! → SUSPICIOUS! ⚠️
```

### 3. Blacklist
```
Key: blacklist:user:user-001
Key: blacklist:merchant:merchant-001
Key: blacklist:ip:192.168.1.1
Type: String
TTL: None (permanent)

Check:
EXISTS blacklist:user:user-001
→ true = BLOCKED ❌
→ false = OK ✅
```

## Redis Configuration
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## RedisTemplate Usage
```java
// Increment counter
Long count = redisTemplate
    .opsForValue()
    .increment(key);

// Set with TTL
redisTemplate.opsForValue()
    .set(key, value,
         Duration.ofSeconds(600));

// Get value
String value = redisTemplate
    .opsForValue()
    .get(key);

// Check key exists
Boolean exists = redisTemplate
    .hasKey(key);
```

## TTL Strategy
```
velocity:{userId}
→ TTL: 600s (10 min window)
→ Auto resets after window
→ No manual cleanup needed ✅

location:{userId}
→ TTL: 3600s (1 hour window)
→ Location can change after 1 hour
→ New baseline set ✅

blacklist entries
→ No TTL
→ Permanent until admin removes
→ Manual management ✅
```

## Redis vs Database

| Feature | Redis | PostgreSQL |
|---------|-------|-----------|
| Speed | 0.1ms ✅ | 5-10ms |
| Storage | RAM | Disk |
| Data loss on crash | Possible | No |
| Persistence | Optional | Yes |
| Use case | Cache/Speed | Persistent |

## Production Considerations
```
Redis Cluster:
→ Sharding across nodes
→ Handles millions of req/sec
→ High availability

Persistence:
→ RDB snapshots
→ AOF logging
→ Data survives restart

Eviction Policy:
→ allkeys-lru
→ Remove least used keys
→ When memory full
```

## Interview Questions

**Q: Why Redis for velocity check?**
```
Redis INCR is atomic operation.
No race conditions.
TTL auto-resets window.
0.1ms vs 10ms database query.
Perfect for high throughput counting.
```

**Q: What happens if Redis crashes?**
```
Velocity counters reset → false negatives
Location data lost → missed location fraud
Blacklist in Redis → need persistence enabled

Production fix:
→ Redis persistence (AOF)
→ Redis cluster for HA
→ Blacklist backup in database
```

**Q: How does TTL work in Redis?**
```
EXPIRE key seconds
→ Key auto-deleted after TTL
→ No manual cleanup needed
→ Perfect for sliding windows
```

**Q: Redis data structures?**
```
String → simple key-value (our use)
Hash → object storage
List → ordered collection
Set → unique members
Sorted Set → ranked members
Bitmap → boolean flags
```