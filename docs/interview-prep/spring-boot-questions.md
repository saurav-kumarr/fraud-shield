# Spring Boot Interview Questions

## Core Spring Boot

### Q1: What is Spring Boot?
```
Spring Boot is opinionated framework
built on top of Spring Framework.

Key features:
→ Auto configuration
→ Embedded server (Tomcat)
→ Starter dependencies
→ Actuator for monitoring
→ No XML configuration needed

Without Spring Boot:
→ Manual bean configuration
→ External server deployment
→ Complex setup ❌

With Spring Boot:
→ @SpringBootApplication
→ main() method
→ Runs immediately ✅
```

### Q2: What is Auto Configuration?
```
Spring Boot reads classpath:
→ If PostgreSQL driver found
  → Auto configure DataSource
→ If Kafka found
  → Auto configure KafkaTemplate
→ If Redis found
  → Auto configure RedisTemplate

No manual configuration needed ✅

@EnableAutoConfiguration
→ Scans for auto config classes
→ Applies if conditions met
```

### Q3: What is @SpringBootApplication?
```
Combination of 3 annotations:

@SpringBootConfiguration
→ Marks as configuration class

@EnableAutoConfiguration
→ Enables auto configuration

@ComponentScan
→ Scans for components
→ @Service, @Repository, @Controller

One annotation = three annotations ✅
```

### Q4: What is Dependency Injection?
```
Spring manages object creation:

Without DI:
TransactionService service =
    new TransactionService(
        new TransactionRepository(),
        new KafkaProducer()
    );

With DI:
@Autowired or @RequiredArgsConstructor
→ Spring injects dependencies
→ Loose coupling ✅
→ Easy testing ✅
```

### Q5: Constructor vs Field Injection?
```
Field Injection ❌:
@Autowired
private TransactionRepository repo;
→ Hard to test
→ Can inject null
→ Not recommended

Constructor Injection ✅:
@RequiredArgsConstructor
private final TransactionRepository repo;
→ Immutable dependencies
→ Easy to test
→ Industry standard
```

---

## Spring Data JPA

### Q6: What is JPA?
```
JPA = Java Persistence API
Maps Java objects to database tables.

Without JPA:
→ Write SQL manually
→ Handle connections
→ Map ResultSet to objects ❌

With JPA:
→ @Entity annotation
→ Spring generates SQL
→ Auto mapping ✅
```

### Q7: What is Spring Data JPA?
```
Extends JPA with repositories.

JpaRepository<Entity, ID> gives:
→ save()
→ findById()
→ findAll()
→ deleteById()
→ count()

No SQL needed! ✅
```

### Q8: What is method name query derivation?
```
Spring generates SQL from method name:

findByUserId(String userId)
→ SELECT * FROM transactions
  WHERE user_id = ?

countByUserIdAndCreatedAtAfter(
    String userId,
    LocalDateTime after)
→ SELECT COUNT(*) FROM transactions
  WHERE user_id = ?
  AND created_at > ?

Magic! ✅
```

### Q9: What is @Transactional?
```
Wraps method in database transaction:

@Transactional
public TransactionResponse createTransaction(...) {
    // Save to DB
    // Publish to Kafka
    // If anything fails → rollback DB ✅
}

Without @Transactional:
→ DB saved but Kafka failed
→ Inconsistent state ❌
```

### Q10: What is ddl-auto?
```
create-drop:
→ Creates tables on start
→ Drops on shutdown
→ Data lost every restart
→ Development testing only

update:
→ Creates if not exists
→ Updates schema
→ Keeps data ✅
→ Production use

validate:
→ Validates schema only
→ No changes
→ Strict production use
```

---

## Spring Security

### Q11: How does Spring Security work?
```
Filter Chain:
Request
  ↓
SecurityFilterChain
  ↓
JwtAuthFilter (our custom)
  ↓ validates JWT
  ↓ sets SecurityContext
  ↓
Controller ✅
```

### Q12: What is SecurityContextHolder?
```
Holds current authenticated user:
→ Available anywhere in request
→ ThreadLocal storage
→ Cleared after request

SecurityContextHolder
    .getContext()
    .getAuthentication()
    → Returns authenticated user
```

### Q13: What is BCrypt?
```
Password hashing algorithm:
→ One-way hash
→ Salt included
→ Cannot reverse

"password123"
→ "$2a$10$abc123..."
→ Different hash each time (salt)

Never store plain passwords! ✅
```

### Q14: What is CSRF and why disabled?
```
CSRF = Cross Site Request Forgery
Attack where malicious site
makes requests on user's behalf.

We disabled because:
→ REST API + JWT
→ Stateless
→ No session cookies
→ CSRF only affects cookie-based auth
→ JWT in Authorization header = safe ✅
```

### Q15: What is SessionCreationPolicy.STATELESS?
```
No server-side sessions:
→ JWT handles state
→ Server doesn't remember clients
→ Any instance handles any request
→ True horizontal scaling ✅

Traditional:
→ Server stores session
→ Sticky sessions needed
→ Not scalable ❌
```

---

## Spring Cloud

### Q16: What is Eureka?
```
Service Discovery:
→ Services register on startup
→ Sends heartbeat every 30s
→ Gateway discovers services
→ Load balancing via lb://

Without Eureka:
→ Hardcode service URLs
→ Not scalable ❌

With Eureka:
→ Dynamic discovery
→ Multiple instances handled ✅
```

### Q17: What is Spring Cloud Gateway?
```
API Gateway:
→ Single entry point
→ JWT validation
→ Request routing
→ CORS configuration
→ Rate limiting

New version uses:
spring-cloud-starter-gateway-server-webmvc
```

### Q18: What is @RefreshScope?
```
Beans recreated on config refresh:

Without @RefreshScope:
→ @Value fields set once on startup
→ Config change ignored ❌

With @RefreshScope:
→ Bean recreated after refresh
→ New @Value applied ✅
→ Zero restart needed
```

### Q19: What is Spring Cloud Config?
```
Centralized configuration:
→ One config server
→ All services fetch config
→ Dynamic refresh via Bus
→ Environment specific configs

Production benefit:
→ Change DB password once
→ All services updated ✅
```

### Q20: What is Spring Cloud Bus?
```
Broadcasts events to all services:

POST /actuator/busrefresh
→ Config Server publishes event
→ Kafka carries event
→ All services receive
→ All services refresh config
→ Zero downtime ✅
```

---

## Lombok

### Q21: What is Lombok?
```
Reduces boilerplate code:

@Data → getters, setters, equals, hashCode
@Builder → builder pattern
@NoArgsConstructor → empty constructor
@AllArgsConstructor → all args constructor
@RequiredArgsConstructor → final fields constructor
@Slf4j → logger instance
```

### Q22: Why @RequiredArgsConstructor over @Autowired?
```
@Autowired (field injection) ❌:
→ Can inject null
→ Hard to test
→ Mutable

@RequiredArgsConstructor ✅:
→ Constructor injection
→ Immutable (final fields)
→ Easy to test
→ Industry standard
```

---

## Actuator

### Q23: What is Spring Boot Actuator?
```
Production monitoring:
→ /actuator/health
→ /actuator/metrics
→ /actuator/info
→ /actuator/busrefresh
→ /actuator/env

Our config:
management.endpoints.web.exposure.include: "*"
→ Expose all endpoints ✅
```