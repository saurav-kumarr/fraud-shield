# Why Spring Cloud Config Server?

## Problem Without Config Server
```
8 microservices each with own application.yml:

transaction-service/application.yml
fraud-detection-service/application.yml
alert-service/application.yml
user-service/application.yml
report-service/application.yml
api-gateway/application.yml
eureka-server/application.yml
config-server/application.yml

Problems:
❌ Change DB password → update 6 files
❌ Change Kafka server → update 6 files
❌ Inconsistency risk
❌ Must restart all services
❌ No audit trail of config changes
```

## Why We Chose Config Server

### 1. Centralized Configuration
```
ONE place for ALL configs:

config-server/src/main/resources/configs/
├── transaction-service.yml
├── fraud-detection-service.yml
├── alert-service.yml
├── user-service.yml
├── report-service.yml
└── api-gateway.yml

Change DB password:
→ Update ONE file ✅
→ All services get update
```

### 2. Dynamic Refresh (Spring Cloud Bus)
```
Without Config Server:
→ Change config
→ Restart ALL services
→ Downtime ❌

With Config Server + Bus:
→ Change config
→ POST /actuator/busrefresh ONCE
→ ALL services refresh automatically
→ Zero downtime ✅
→ Zero restarts ✅
```

### 3. Environment Specific Configs
```
Development:
→ configs/transaction-service.yml

Production:
→ configs/transaction-service-prod.yml

Staging:
→ configs/transaction-service-staging.yml

Same code → different configs
per environment ✅
```

### 4. Security
```
Sensitive values:
→ ${JWT_SECRET} → environment variable
→ ${DB_PASSWORD} → environment variable

Config Server serves placeholders
Actual values from environment ✅
Never in code or Git ✅
```

## How It Works
```
Service starts
    ↓
Reads local application.yml:
→ spring.application.name: transaction-service
→ spring.config.import: configserver:http://localhost:8888
    ↓
Calls Config Server:
GET http://localhost:8888/transaction-service/default
    ↓
Config Server returns full config
    ↓
Service starts with merged config ✅
```

## Spring Cloud Bus Flow
```
Developer changes configs/alert-service.yml
    ↓
POST http://localhost:8888/actuator/busrefresh
    ↓
Config Server publishes to Kafka "springCloudBus"
    ↓
ALL services receive RefreshEvent
    ↓
Each service fetches new config
    ↓
@RefreshScope beans recreated
    ↓
New config applied → Zero downtime ✅
```

## Config Server vs Alternatives

| Feature | Config Server | Hardcoded YML | AWS SSM |
|---------|--------------|---------------|---------|
| Centralized | Yes ✅ | No ❌ | Yes ✅ |
| Dynamic refresh | Yes ✅ | No ❌ | No |
| Spring integration | Native ✅ | Native | Extra setup |
| Cost | Free ✅ | Free | Paid |

## Interview Answer

> "We use Spring Cloud Config Server to centralize
> all microservice configurations. Instead of 8
> separate application.yml files, we have one config
> repository serving all services. The killer feature
> is dynamic refresh via Spring Cloud Bus — when
> we change a configuration, one POST to
> /actuator/busrefresh refreshes ALL services
> simultaneously through Kafka without any restarts.
> This means zero downtime configuration updates.
> Sensitive values like JWT secrets and database
> passwords are stored as environment variable
> placeholders, never in the config files themselves."