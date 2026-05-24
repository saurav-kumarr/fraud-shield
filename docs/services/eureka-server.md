# Eureka Server

## Overview
Service discovery server. All microservices register themselves here to enable service-to-service communication.

## Port
`8761`

## Responsibilities
- Service registration
- Service discovery
- Health monitoring of registered services
- Load balancing support (with Spring Cloud LoadBalancer)

## How Services Register
Each microservice has:
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## Dashboard
- `http://localhost:8761`
- Shows all registered services
- Service health status
- Instance counts

## Registered Services
- api-gateway
- transaction-service
- fraud-detection-service
- alert-service
- user-service
- report-service

## Configuration
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```