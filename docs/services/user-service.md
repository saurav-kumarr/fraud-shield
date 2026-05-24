# User Service

## Overview
Manages user authentication and registration. Generates JWT tokens for authenticated users.

## Port
`8084`

## Database
- PostgreSQL (`user_db`)

## Responsibilities
- User registration with BCrypt password hashing
- User authentication via Spring Security
- JWT token generation
- Role-based user management

## REST Endpoints
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login & get JWT | Public |

## Request/Response

### Register Request
```json
{
    "firstName": "Saurav",
    "lastName": "Kumar",
    "email": "saurav@gmail.com",
    "password": "password123"
}
```

### Login Request
```json
{
    "email": "saurav@gmail.com",
    "password": "password123"
}
```

### Auth Response
```json
{
    "token": "eyJhbGc...",
    "email": "saurav@gmail.com",
    "firstName": "Saurav",
    "lastName": "Kumar",
    "role": "USER",
    "expiresIn": 86400000
}
```

## JWT Configuration
- Algorithm: **HS384** (HMAC SHA-384)
- Expiration: 24 hours
- Claims: `email`, `role`, `firstName`, `lastName`
- Secret: Environment variable `JWT_SECRET`

## Security
- Spring Security with JWT
- BCrypt password encoder
- Stateless sessions
- Roles: USER, ADMIN, MERCHANT, ANALYST

## Database Schema
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(20),
    enabled BOOLEAN,
    created_at TIMESTAMP
);
```

## Health Check
- `GET http://localhost:8084/actuator/health`