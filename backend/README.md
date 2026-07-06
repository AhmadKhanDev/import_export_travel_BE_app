# Traveller-Buyer Marketplace Backend

A production-ready modular monolith Spring Boot backend for connecting buyers who need items delivered internationally with travellers who can carry them.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Cache / Rate Limiting | Redis 7 |
| Async Events | Apache Kafka |
| ORM | Spring Data JPA + Hibernate |
| Code Gen | Lombok, MapStruct |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Monitoring | Spring Boot Actuator + Micrometer |
| Containerization | Docker + Docker Compose |
| CI | GitHub Actions |

---

## Modules Completed

1. Auth (register, login, logout, token refresh, JWT blacklist)
2. Profile + KYC
3. Listing (Buyer Requests + Traveller Trips)
4. Matching
5. Offer & Booking
6. Payment & Escrow
7. Delivery Verification
8. Notification
9. Review, Rating & Dispute
10. Chat
11. Admin & Operations
12. Infrastructure / Production (Redis, Kafka, Rate Limiting, Correlation ID, Actuator, Metrics, CI)

---

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose

---

## Local Setup

### 1. Clone and configure environment

```bash
cd backend
cp .env.example .env
# Edit .env with your preferred values
```

### 2. Start infrastructure with Docker Compose

```bash
docker compose up -d postgres redis kafka zookeeper
```

This starts:
- PostgreSQL on port `5432`
- Redis on port `6379`
- Zookeeper on port `2181`
- Kafka on port `9092`

Wait for services to be healthy:

```bash
docker compose ps
```

### 3. Run the backend

With local profile (recommended for development):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or using environment variables directly:

```bash
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

### 4. Access the API

| URL | Description |
|---|---|
| http://localhost:8080 | API base |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api-docs | OpenAPI JSON |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/actuator/metrics | Metrics |

---

## Docker Compose Setup

### Start all services

```bash
docker compose up -d
```

### Start infrastructure only (run backend locally)

```bash
docker compose up -d postgres redis kafka zookeeper
```

### Stop everything

```bash
docker compose down
```

### Stop and remove volumes (full reset)

```bash
docker compose down -v
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_DB` | `marketplace` | PostgreSQL database name |
| `POSTGRES_USER` | `marketplace` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `marketplace` | PostgreSQL password |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `JWT_SECRET` | dev default | JWT signing secret (min 32 chars) |
| `DELIVERY_CODE_SECRET` | dev default | HMAC secret for delivery codes |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `KAFKA_ENABLED` | `true` | Enable/disable Kafka publishing |
| `SERVER_PORT` | `8080` | Backend HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,...` | Allowed CORS origins |
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |

> **Never commit real secrets.** Use `.env` (gitignored) or a secrets manager.

---

## Spring Profiles

| Profile | Usage |
|---|---|
| `local` | Local development with Docker Compose |
| `dev` | Development/staging server |
| `prod` | Production (all secrets from env vars) |

Run with a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

If port `8080` is already in use, stop stale dev processes first:

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\stop-dev-ports.ps1
```

---

## Database Migrations

Flyway runs automatically on startup. Migration scripts are in `src/main/resources/db/migration/`.

To run migrations manually:

```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/marketplace \
  -Dflyway.user=marketplace -Dflyway.password=marketplace
```

---

## Redis Usage

Redis is used for:

- **JWT Blacklist**: On logout, the access token hash is stored in Redis with its remaining TTL. The JWT filter rejects blacklisted tokens.
- **Rate Limiting**: Request counters per endpoint/IP/user.
- **Delivery Attempt Counter**: Tracks wrong delivery code attempts (max 5 per 10 minutes).
- **Caching**: Rating summaries, dashboard counters (short TTL).

---

## Kafka Usage

Kafka is used for async domain events. Events are published from important service points:

- `USER_REGISTERED`, `KYC_SUBMITTED/APPROVED/REJECTED`
- `OFFER_SENT/ACCEPTED`, `BOOKING_CREATED`
- `PAYMENT_HELD/RELEASED/REFUNDED`
- `DELIVERY_CODE_GENERATED`, `DELIVERY_VERIFIED`
- `DISPUTE_CREATED/RESOLVED`

**Disable Kafka** (for simpler local dev without Kafka running):

```yaml
# application-local.yml
app:
  kafka:
    enabled: false
```

Or via environment variable:
```bash
KAFKA_ENABLED=false mvn spring-boot:run
```

Topics are auto-created. See `KafkaTopics.java` for topic names.

---

## Rate Limiting

Rate limits are enforced via Redis counters. Defaults (configurable in `application.yml`):

| Endpoint | Limit |
|---|---|
| POST /api/v1/auth/login | 5 req/min/IP |
| POST /api/v1/auth/register | 3 req/min/IP |
| Delivery code generate | 3 req/10min/booking/user |
| Delivery code verify | 5 req/10min/booking/user |
| General API | 100 req/min/IP |

Returns HTTP `429 Too Many Requests` when exceeded.

---

## Actuator Health Checks

```bash
# Basic health
curl http://localhost:8080/actuator/health

# All metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/auth.login.success.count
```

Health indicators: PostgreSQL, Redis, Kafka.

---

## Request Tracing (Correlation ID)

Every request gets a `X-Correlation-ID` header:
- If the client provides one, it is used.
- If not, a UUID is generated server-side.
- The correlation ID appears in response headers and in all log lines.
- Error responses include `correlationId`.

Test with:

```bash
curl -H "X-Correlation-ID: my-trace-123" http://localhost:8080/api/v1/auth/me
```

---

## Common API Flow

### 1. Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"buyer@example.com","password":"Pass1234!","fullName":"Ali Khan","phoneNumber":"+447700900000","role":"BUYER"}'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"buyer@example.com","password":"Pass1234!"}'
```

Save the `accessToken` from the response.

### 3. KYC Submission (Traveller)

```bash
curl -X POST http://localhost:8080/api/v1/kyc/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"documentType":"PASSPORT","documentNumber":"AB123456","documentCountry":"GB","frontImageUrl":"https://storage.example.com/front.jpg"}'
```

### 4. Create Buyer Request

```bash
curl -X POST http://localhost:8080/api/v1/buyer-requests \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"iPhone 15 Pro","description":"Buy from Apple UK","itemCategory":"ELECTRONICS","itemWeight":0.3,"estimatedPrice":1200.00,"rewardAmount":100.00,"sourceCountry":"GB","sourceCity":"London","destinationCountry":"PK","destinationCity":"Karachi","requiredByDate":"2026-08-01T00:00:00Z"}'
```

### 5. Match → Offer → Booking → Payment → Delivery → Review

See full API documentation at http://localhost:8080/swagger-ui.html

---

## Build

```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package JAR
mvn clean package -DskipTests

# Docker build
docker build -t marketplace-backend .
```

---

## Production Deployment Notes

See [`docs/deployment.md`](docs/deployment.md) for full deployment instructions covering AWS EC2, RDS, ElastiCache, MSK, and secrets management.

---

## Future Improvements

- WebSocket real-time chat
- Email notifications (SendGrid/SES)
- SMS notifications (Twilio)
- Payment provider integration (Stripe/PayPal)
- Outbox pattern for reliable event delivery
- Full-text search (Elasticsearch)
- File upload (S3)
- Admin React frontend
- Buyer/Traveller mobile apps
- Multi-currency support
