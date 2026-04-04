# Why JWT Authentication?

## Problem with Session Based Auth
```
Traditional session authentication:

Client logs in
→ Server creates session
→ Stores in memory/database
→ Returns session ID

Every request:
→ Client sends session ID
→ Server looks up session in DB
→ 5-10ms database hit per request ❌

Problems:
❌ Server must store sessions
❌ Database hit every request
❌ Not scalable horizontally
❌ Session sharing between services complex
```

## Why We Chose JWT

### 1. Stateless
```
JWT contains all user info:
→ Email
→ Role
→ Expiry

Server validates signature:
→ No database lookup needed
→ No session storage needed
→ 0ms overhead ✅
```

### 2. Microservices Friendly
```
Multiple services need auth:
→ transaction-service
→ report-service
→ alert-service

With Session:
→ Each service needs session store access
→ Complex shared infrastructure ❌

With JWT:
→ Each service validates token independently
→ Just needs secret key
→ No shared infrastructure ✅
```

### 3. Scalability
```
10 transaction-service instances:
→ Any instance validates same JWT
→ No sticky sessions needed
→ True horizontal scaling ✅
```

### 4. Validation at Gateway
```
API Gateway validates JWT:
→ Invalid token rejected immediately
→ Never reaches backend services
→ Single security checkpoint ✅
→ Backend services trust gateway
```

## JWT Structure
```
eyJhbGciOiJIUzM4NCJ9     ← Header (Base64)
.
eyJzdWIiOiJ1c2VyQGdtYWls  ← Payload (Base64)
.
SflKxwRJSMeKKF2QT4fwpMeJ  ← Signature (HMAC)

Header decoded:
{
  "alg": "HS384"
}

Payload decoded:
{
  "sub": "user@gmail.com",
  "iat": 1774195542,
  "exp": 1774281942
}
```

## Security Measures
```
→ Secrets stored in environment variables
→ Never in code or yml files
→ HMAC-SHA384 signing algorithm
→ 24 hour expiration
→ BCrypt password hashing ✅
```

## JWT vs Session

| Feature | JWT | Session |
|---------|-----|---------|
| Stateless | Yes ✅ | No |
| DB lookup per request | No ✅ | Yes |
| Horizontal scaling | Easy ✅ | Complex |
| Microservices | Perfect ✅ | Complex |
| Token revocation | Complex | Easy |

## Token Revocation Challenge
```
JWT weakness:
→ Cannot revoke before expiry
→ Stolen token valid until expiry

Our mitigation:
→ Short expiry (24 hours)
→ HTTPS only in production
→ Secure token storage in client

Production enhancement:
→ Token blacklist in Redis
→ Refresh token pattern
→ We can add later ✅
```

## Interview Answer

> "We use JWT for stateless authentication because
> our microservices architecture demands it. With
> multiple service instances, session-based auth
> requires shared session storage which creates
> infrastructure complexity. JWT tokens are
> self-contained — each service validates the
> signature independently without any database
> lookup. We validate JWT at the API Gateway level
> so invalid tokens never reach backend services.
> Secrets are stored in environment variables
> locally and AWS Secrets Manager in production,
> never in code or configuration files."