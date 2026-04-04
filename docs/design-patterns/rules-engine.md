# Rules Engine Design Pattern

## Problem Without Rules Engine
```java
// Without pattern - messy if/else ❌
public String detectFraud(Transaction t) {
    if (isBlacklisted(t.getUserId())) {
        return "BLOCKED";
    }
    if (getCount(t.getUserId()) > 5) {
        return "BLOCKED";
    }
    if (t.getAmount() > 100000) {
        return "FLAGGED";
    }
    if (locationChanged(t.getUserId())) {
        return "BLOCKED";
    }
    // 100 more conditions...
    // Unmaintainable! ❌
}
```

## Solution - Rules Engine Pattern

### Patterns Used
```
1. Strategy Pattern
   → Each rule is a strategy
   → Interchangeable algorithms

2. Chain of Responsibility
   → Rules execute in sequence
   → Short-circuit on high risk
```

### Implementation
```java
// Interface - Strategy Pattern
public interface FraudRule {
    RuleResult evaluate(TransactionEvent transaction);
    String getRuleName();
    int getPriority();
}

// Concrete Rules
@Component
public class BlacklistRule implements FraudRule {
    public int getPriority() { return 1; } // Runs first
    public RuleResult evaluate(TransactionEvent t) {
        // Check Redis blacklist
    }
}

@Component
public class VelocityRule implements FraudRule {
    public int getPriority() { return 2; }
    public RuleResult evaluate(TransactionEvent t) {
        // Check Redis counter
    }
}

// Engine - Chain of Responsibility
@Component
public class FraudDetectionEngine {
    private final List<FraudRule> fraudRules;

    public EngineResult analyze(TransactionEvent t) {
        List<RuleResult> results = fraudRules.stream()
            .sorted(Comparator.comparingInt(
                FraudRule::getPriority))
            .map(rule -> rule.evaluate(t))
            .toList();

        double maxScore = results.stream()
            .mapToDouble(RuleResult::getRiskScore)
            .max()
            .orElse(0.0);

        return determineVerdict(maxScore);
    }
}
```

## Rules Execution Flow
```
Transaction arrives
    ↓
BlacklistRule (priority 1)
→ Score: 100 → SHORT CIRCUIT → BLOCKED
    ↓ (if not blacklisted)
VelocityRule (priority 2)
→ Score: 80 → continue
    ↓
AmountRule (priority 3)
→ Score: 70 → continue
    ↓
LocationRule (priority 4)
→ Score: 90 → continue
    ↓
Max Score = 90 → BLOCKED
```

## Short Circuit Logic
```java
for (FraudRule rule : sortedRules) {
    RuleResult result = rule.evaluate(transaction);
    results.add(result);

    // Stop if high risk detected
    if (result.getRiskScore() >= BLOCK_THRESHOLD) {
        log.info("Short-circuit by: {}",
            rule.getRuleName());
        break; // Skip remaining rules ✅
    }
}
```

## Benefits
```
✅ Open/Closed Principle
→ Add new rule = new class only
→ No existing code changed

✅ Single Responsibility
→ Each rule has one job
→ Easy to test individually

✅ Configurable
→ Rule weights tunable
→ Thresholds in config server

✅ Testable
→ Each rule tested independently
→ Engine tested separately
```

## Adding New Rule
```java
// Just add new class! ✅
@Component
public class DeviceRule implements FraudRule {

    @Override
    public RuleResult evaluate(TransactionEvent t) {
        // Check device fingerprint
        return RuleResult.passed(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "DEVICE_RULE";
    }

    @Override
    public int getPriority() {
        return 5; // Runs last
    }
}
// Spring auto-discovers and injects it!
// No other code changes needed ✅
```

## Risk Scoring
```
BlacklistRule  → 100 (immediate block)
LocationRule   → 90  (high risk)
VelocityRule   → 80  (high risk)
AmountRule     → 70/40 (high/medium)

Thresholds:
Score >= 70 → BLOCKED
Score >= 40 → FLAGGED
Score < 40  → APPROVED
```

## Interview Answer

> "I implemented a custom rules engine using
> Strategy and Chain of Responsibility patterns.
> Each fraud rule implements the FraudRule interface
> with evaluate(), getRuleName() and getPriority()
> methods. The engine sorts rules by priority,
> executes them in sequence, and short-circuits
> when a high-risk score is detected — avoiding
> unnecessary rule evaluation. Adding a new fraud
> rule requires only creating a new class implementing
> FraudRule. Spring automatically discovers and
> injects it into the engine. This follows the
> Open/Closed principle — open for extension,
> closed for modification."