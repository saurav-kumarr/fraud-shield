# Why PostgreSQL and MongoDB?

## Polyglot Persistence
```
Using multiple databases:
→ Each optimized for its use case
→ No single database fits all needs
→ Industry standard in fintech ✅
```

---

## PostgreSQL

### Why PostgreSQL for Transactions?
```
Financial transactions need ACID:

Atomicity:
→ Transaction saves completely or not at all
→ No partial saves ✅

Consistency:
→ Database always in valid state
→ Constraints enforced ✅

Isolation:
→ Concurrent transactions don't interfere
→ No dirty reads ✅

Durability:
→ Committed data never lost
→ Survives crashes ✅
```

### Tables in PostgreSQL
```
transactions  → transaction-service
users         → user-service
alerts        → alert-service
fraud_reports → report-service
```

### Why Not MongoDB for Transactions?
```
MongoDB:
→ Eventually consistent
→ No multi-document ACID (without replica set)
→ Financial data needs strict consistency
→ Not suitable for transaction history ❌

PostgreSQL:
→ Full ACID compliance
→ Perfect for financial data ✅
→ SQL queries for analytics
→ Joins for complex reports ✅
```

---

## MongoDB

### Why MongoDB for Fraud Patterns?
```
Fraud patterns have flexible structure:

Velocity Pattern:
{
  patternType: "VELOCITY",
  userId: "123",
  txnCount: 10,
  windowMinutes: 10
}

Location Pattern:
{
  patternType: "LOCATION",
  userId: "456",
  fromLocation: "Delhi",
  toLocation: "Mumbai",
  timeDiff: "30min"
}

Different patterns = different fields
MongoDB handles this perfectly ✅
PostgreSQL would need many nullable columns ❌
```

### Collections in MongoDB
```
fraud_patterns → fraud-detection-service
                 report-service
```

### Why Not PostgreSQL for Patterns?
```
PostgreSQL approach ❌:
CREATE TABLE fraud_patterns (
  id BIGINT,
  pattern_type VARCHAR,
  velocity_count INT NULL,      ← nullable
  location_from VARCHAR NULL,   ← nullable
  location_to VARCHAR NULL,     ← nullable
  amount_threshold DECIMAL NULL ← nullable
  ...many more nullable columns
)

MongoDB approach ✅:
→ Store only relevant fields
→ Schema evolves with new patterns
→ No migration needed
→ Flexible and clean
```

---

## Decision Summary

| Data Type | Database | Reason |
|-----------|---------|--------|
| Transactions | PostgreSQL | ACID compliance |
| Users | PostgreSQL | Relational, joins |
| Alerts | PostgreSQL | Structured, analytics |
| Reports | PostgreSQL | SQL aggregations |
| Fraud Patterns | MongoDB | Flexible schema |

---

## Real World Reference
```
Razorpay:
→ PostgreSQL for payment records
→ MongoDB for analytics data

PayPal:
→ Multiple databases per use case
→ Polyglot persistence ✅
```

---

## Interview Answer

> "We use polyglot persistence — PostgreSQL for
> structured financial data and MongoDB for fraud
> patterns. PostgreSQL gives us full ACID compliance
> for transaction records where data integrity is
> critical. MongoDB is used for fraud patterns because
> different pattern types have different attributes —
> a velocity pattern has different fields than a
> location pattern. Using MongoDB eliminates the need
> for nullable columns and allows our pattern schema
> to evolve as we detect new fraud types. This is
> exactly how companies like Razorpay separate their
> transactional and analytical data stores."