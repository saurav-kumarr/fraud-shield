# API Gateway Service

## Overview
Single entry point for all client requests. Routes traffic to backend microservices, handles authentication, rate limiting, and CORS.

## Port
`8080`

## Responsibilities
- JWT token validation
- Request routing to microservices
- Rate limiting (tiered: burst + sustained)
- CORS handling
- Adding user context headers to downstream services

## Tech Stack
- Spring Cloud Gateway (WebMVC)
- Spring Boot 3.5.11
- Bucket4j for rate limiting
- JWT (HS384)

## Routes
| Path | Target Service |
|------|----------------|
| `/api/auth/**` | user-service |
| `/api/users/**` | user-service |
| `/api/transactions/**` | transaction-service |
| `/api/fraud/**` | fraud-detection-service |
| `/api/alerts/**` | alert-service |
| `/api/reports/**` | report-service |

## Filters

### JwtAuthenticationFilter (Order 1)
- Validates JWT token
- Extracts user info from claims
- Adds headers: `X-User-Id`, `X-User-Email`, `X-User-Role`
- Skips public URLs: `/api/auth/**`, `/actuator`, `/ws`

### RateLimitFilter (Order 2)
- Per-user rate limiting using Bucket4j
- Two tiers:
    - Burst: short-term spike protection
    - Sustained: long-term quota

#### Rate Limits per Role
| Role | Burst (req/sec) | Sustained (req/min) |
|------|----------------|---------------------|
| ADMIN | 50 | 10000 |
| MERCHANT | 20 | 1000 |
| ANALYST | 15 | 500 |
| USER | 10 | 100 |

## Response Headers
- `X-Rate-Limit-Remaining`: Tokens left in bucket
- `X-Rate-Limit-Retry-After`: Seconds until refill (on 429)

## Security
- JWT secret stored as environment variable `JWT_SECRET`
- Never in YAML or Git
- Token validation only at Gateway (centralized)

## Health Check
- `GET http://localhost:8080/actuator/health`