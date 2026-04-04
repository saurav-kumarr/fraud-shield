# Why Redis?

## Problem Without Redis
```
Database approach for fraud rules:

Every transaction check:
→ SELECT * FROM fraud_rules WHERE...
→ 5-10ms per query
→ Database connection pool limited
→ Cannot handle 1M req/sec ❌

Velocity check:
→ SELECT COUNT(*) FROM transactions
  WHERE user_id = ? AND created_at > ?
→ 10-20ms per query
→ Database load increases ❌
```

## Why We Chose Redis

### 1. Sub-millisecond Response
```
Redis stores data in RAM:
→ 0.1ms per lookup
→ 100x faster than database

For 1M transactions/second:
Database: 10ms × 1M = 2.7 hours ❌
Redis:    0.1ms × 1M = 100 seconds ✅
```

### 2. Velocity Rule
```
Redis INCR command:
→ Atomic operation
→ Thread safe
→ No race conditions

Key: velocity:user-001
→ Increments counter
→ TTL: 600 seconds (10 min)
→ Auto resets after window ✅

If count > 5 in 10 min → fraud!
```

### 3. Location Tracking
```
Key: location:user-001
Value: "Delhi, India"
TTL: 3600 seconds (1 hour)

Transaction from different location
within 1 hour → suspicious! ✅
```

### 4. Blacklist Cache
```
Key: blacklist:user:user-001
Key: blacklist:merchant:merchant-001
Key: blacklist:ip:192.168.1.1

O(1) lookup time
Instant blacklist check ✅
```

## Redis Data Structures Used

| Structure | Use Case | Key Pattern |
|-----------|---------|-------------|
| String | Velocity counter | velocity:{userId} |
| String | Location tracking | location:{userId} |
| String | Blacklist entries | blacklist:{type}:{id} |

## Redis vs Database

| Feature | Redis | PostgreSQL |
|---------|-------|-----------|
| Speed | 0.1ms ✅ | 5-10ms |
| Storage | RAM | Disk |
| Persistence | Optional | Yes |
| Data types | Rich | SQL |
| Use case | Cache/Speed | Persistent data |

## TTL Strategy
```
velocity:{userId}  → TTL: 600s (10 min window)
location:{userId}  → TTL: 3600s (1 hour window)
blacklist entries  → No TTL (permanent until removed)
```

## Interview Answer

> "We use Redis for three critical fraud detection
> operations. First, velocity checking — Redis atomic
> INCR with TTL implements sliding window rate limiting
> at sub-millisecond speed without any database queries.
> Second, location tracking — we store last known
> transaction location with 1-hour TTL to detect
> impossible travel patterns. Third, blacklist lookups
> — O(1) Redis key existence checks for blacklisted
> users, merchants and IPs. This gives us sub-millisecond
> fraud rule evaluation compared to 5-10ms database
> queries, making real-time fraud detection at scale
> possible."