# Traveller-Buyer Marketplace Backend

Modular monolith Spring Boot backend.

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose

## Quick Start

Start PostgreSQL:

```bash
cd backend
docker compose up -d
```

Run the application:

```bash
mvn spring-boot:run
```

API: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

## Auth Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/v1/auth/register | Public | Register buyer or traveller |
| POST | /api/v1/auth/login | Public | Login |
| POST | /api/v1/auth/refresh | Public | Refresh access token |
| POST | /api/v1/auth/logout | JWT | Revoke refresh token |
| GET | /api/v1/auth/me | JWT | Current user |

See docs/auth-api-samples.md for curl examples.
