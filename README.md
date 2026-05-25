# fraud-shield
🛡️ Real-time fraud detection system built with Spring Boot Microservices, Kafka, Redis, PostgreSQL, MongoDB, Docker &amp; AWS | Event-driven architecture with React dashboard


---

## 🔄 Event Flow
```
1. POST /api/transactions (with JWT)
2. API Gateway validates JWT
3. Transaction saved to PostgreSQL
4. TransactionEvent → Kafka [transaction-events]
5. Fraud Detection runs Rules Engine
6. FraudAlertEvent → Kafka [fraud-alerts]
7. Alert Service → WebSocket + Email
8. Report Service → PostgreSQL + MongoDB
```

---

## ⚙️ Dynamic Config Refresh
```bash
# Change any config in configs/ folder
# Then hit:
POST http://localhost:8888/actuator/busrefresh

# All services refresh automatically!
# Zero restarts needed ✅
```

---

## 📊 Monitoring
```
Grafana:    http://localhost:3000
Prometheus: http://localhost:9090
Eureka:     http://localhost:8761
```

---

## 📚 Documentation

| Document | Description |
|---------|-------------|
| [HLD](docs/architecture/HLD.md) | High level design |
| [LLD](docs/architecture/LLD.md) | Low level design |
| [Why Microservices](docs/architecture/decisions/why-microservices.md) | Architecture decision |
| [Why Kafka](docs/architecture/decisions/why-kafka.md) | Kafka choice |
| [Why Redis](docs/architecture/decisions/why-redis.md) | Redis choice |
| [Rules Engine](docs/design-patterns/rules-engine.md) | Design pattern |
| [SAGA Pattern](docs/design-patterns/saga-pattern.md) | SAGA implementation |
| [System Design Q&A](docs/interview-prep/system-design-qa.md) | Interview prep |
| [Kafka Questions](docs/interview-prep/kafka-questions.md) | Kafka interview |
| [Spring Boot Questions](docs/interview-prep/spring-boot-questions.md) | Spring interview |

---

## 📚 Service Documentation
- [API Gateway](docs/services/api-gateway.md)
- [Transaction Service](docs/services/transaction-service.md)
- [Fraud Detection Service](docs/services/fraud-detection-service.md)
- [Alert Service](docs/services/alert-service.md)
- [User Service](docs/services/user-service.md)
- [Report Service](docs/services/report-service.md)
- [Eureka Server](docs/services/eureka-server.md)
- [Config Server](docs/services/config-server.md)

---

## 👨‍💻 Author

**Saurav Kumar**
- Backend Engineer | Fintech Domain
- GitHub: [saurav-kumarr](https://github.com/saurav-kumarr)
- LinkedIn: [Saurav Kumar](https://www.linkedin.com/in/saurav-kumar-java-developer)
- Instagram: [Saurav Chaudhary](https://www.instagram.com/sauravchaudharii/)

---

## 📄 License

MIT License - see [LICENSE](LICENSE) for details