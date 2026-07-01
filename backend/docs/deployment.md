# Deployment Guide

This document covers deploying the Traveller-Buyer Marketplace backend to production using AWS services.

---

## Architecture Overview

```
Internet
    │
    ▼
Application Load Balancer (ALB)
    │
    ▼
EC2 / ECS running Docker container (Spring Boot app)
    │
    ├──► RDS PostgreSQL (primary DB)
    ├──► ElastiCache Redis (cache + rate limiting + JWT blacklist)
    └──► Amazon MSK (Kafka managed service)
```

---

## 1. EC2 Deployment with Docker

### Prerequisites

- Ubuntu 22.04 LTS EC2 instance (t3.medium recommended minimum)
- Security group: inbound 8080 (or 443 with ALB), SSH 22
- IAM role with Secrets Manager read access

### Install Docker on EC2

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
newgrp docker
```

### Pull and run the application

```bash
# Pull your Docker image from ECR or Docker Hub
docker pull your-registry/marketplace-backend:latest

# Or build on the server (not recommended for production)
git clone https://github.com/your-org/marketplace.git
cd marketplace/backend
docker build -t marketplace-backend:latest .

# Run with environment variables
docker run -d \
  --name marketplace-backend \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-rds-endpoint.rds.amazonaws.com \
  -e DB_PORT=5432 \
  -e POSTGRES_DB=marketplace \
  -e POSTGRES_USER=marketplace \
  -e POSTGRES_PASSWORD=your-secure-password \
  -e JWT_SECRET=your-256-bit-production-secret \
  -e DELIVERY_CODE_SECRET=your-delivery-secret \
  -e REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com \
  -e REDIS_PORT=6379 \
  -e KAFKA_BOOTSTRAP_SERVERS=b-1.your-msk-cluster.kafka.eu-west-1.amazonaws.com:9092 \
  -e CORS_ALLOWED_ORIGINS=https://yourfrontend.com \
  marketplace-backend:latest
```

---

## 2. RDS PostgreSQL Setup

### Create RDS instance

1. Go to AWS RDS → Create database
2. Engine: PostgreSQL 16
3. Instance class: db.t3.medium (production: db.r6g.large)
4. Storage: 20 GB SSD, enable autoscaling
5. Enable Multi-AZ for high availability
6. Set DB name: `marketplace`, master username, strong password
7. VPC: same VPC as your EC2 instances
8. Security group: allow inbound 5432 from EC2 security group only

### Connection

```
DB_HOST=your-instance.xxxx.eu-west-1.rds.amazonaws.com
DB_PORT=5432
POSTGRES_DB=marketplace
POSTGRES_USER=marketplace
POSTGRES_PASSWORD=from-secrets-manager
```

### Run Flyway migrations

Flyway runs automatically on startup. For manual migration:

```bash
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://your-rds-host:5432/marketplace \
  -Dflyway.user=marketplace \
  -Dflyway.password=your-password
```

---

## 3. ElastiCache Redis Setup

### Create ElastiCache cluster

1. Go to ElastiCache → Create cluster
2. Engine: Redis, version 7.x
3. Node type: cache.t3.micro (production: cache.r6g.large)
4. Number of replicas: 1 (for HA)
5. VPC: same VPC as EC2
6. Security group: allow inbound 6379 from EC2 security group only

### Connection

```
REDIS_HOST=your-cluster.xxxx.cache.amazonaws.com
REDIS_PORT=6379
```

> Note: ElastiCache does not require a password by default in VPC. Enable AUTH token for extra security.

---

## 4. Amazon MSK (Managed Kafka)

### Create MSK cluster

1. Go to Amazon MSK → Create cluster
2. Kafka version: 3.5.x
3. Broker instance type: kafka.t3.small (production: kafka.m5.large)
4. Number of brokers: 3 (for HA)
5. Storage: 100 GB per broker
6. VPC: same VPC as EC2
7. Security group: allow inbound 9092 from EC2 security group

### Connection

```
KAFKA_BOOTSTRAP_SERVERS=b-1.your-cluster.kafka.eu-west-1.amazonaws.com:9092,b-2.your-cluster.kafka.eu-west-1.amazonaws.com:9092
```

### Create topics (if not auto-created)

```bash
/opt/kafka/bin/kafka-topics.sh --bootstrap-server $BROKERS --create --topic marketplace.user.events --replication-factor 3 --partitions 3
/opt/kafka/bin/kafka-topics.sh --bootstrap-server $BROKERS --create --topic marketplace.booking.events --replication-factor 3 --partitions 3
# Repeat for all topics
```

---

## 5. Secrets Management with AWS Secrets Manager

Store all sensitive values in AWS Secrets Manager, never in environment variables directly (use SSM or Secrets Manager integration).

### Store secrets

```bash
aws secretsmanager create-secret \
  --name "marketplace/prod/jwt-secret" \
  --secret-string '{"JWT_SECRET":"your-256-bit-production-secret"}'

aws secretsmanager create-secret \
  --name "marketplace/prod/db-password" \
  --secret-string '{"POSTGRES_PASSWORD":"your-db-password"}'
```

### Fetch at runtime

Use the AWS SDK or Spring Cloud AWS to inject secrets:

```yaml
# With Spring Cloud AWS Secrets Manager (add dependency if needed)
spring:
  config:
    import: "aws-secretsmanager:/marketplace/prod/"
```

Or use an entrypoint script in Docker:

```bash
#!/bin/sh
export JWT_SECRET=$(aws secretsmanager get-secret-value \
  --secret-id marketplace/prod/jwt-secret \
  --query SecretString --output text | jq -r .JWT_SECRET)
exec java -jar app.jar
```

---

## 6. Environment Variables Configuration

### Production `.env` equivalent (set via your deployment system, not a file)

```
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your-rds.rds.amazonaws.com
DB_PORT=5432
POSTGRES_DB=marketplace
POSTGRES_USER=marketplace
POSTGRES_PASSWORD=<from Secrets Manager>
JWT_SECRET=<from Secrets Manager>
DELIVERY_CODE_SECRET=<from Secrets Manager>
REDIS_HOST=your-elasticache.cache.amazonaws.com
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=b-1.your-msk.amazonaws.com:9092
CORS_ALLOWED_ORIGINS=https://yourfrontend.com
```

---

## 7. Running Migrations Safely

Always run Flyway migrations before deploying new application versions:

1. Take a database snapshot/backup first
2. Run Flyway in a migration-only mode:
   ```bash
   java -jar app.jar --spring.flyway.enabled=true --spring.jpa.hibernate.ddl-auto=validate
   ```
3. Deploy the application only after migrations succeed

---

## 8. Monitoring Logs

### CloudWatch Logs

Configure the Docker container to send logs to CloudWatch:

```bash
docker run -d \
  --log-driver=awslogs \
  --log-opt awslogs-region=eu-west-1 \
  --log-opt awslogs-group=/marketplace/backend \
  --log-opt awslogs-stream=app \
  marketplace-backend:latest
```

### View logs

```bash
aws logs tail /marketplace/backend --follow
```

### Correlation ID filtering

Every request has a `X-Correlation-ID`. To trace a specific request:

```bash
aws logs filter-log-events \
  --log-group-name /marketplace/backend \
  --filter-pattern "correlationId=abc-123-xyz"
```

---

## 9. Health Checks and ALB

Configure ALB target group health check:

- Path: `/actuator/health`
- Port: 8080
- Expected HTTP status: 200
- Healthy threshold: 2
- Unhealthy threshold: 3
- Interval: 30 seconds

Readiness/liveness probes (if using ECS or Kubernetes):

```yaml
# ECS task definition health check
healthCheck:
  command: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health/readiness || exit 1"]
  interval: 30
  timeout: 5
  retries: 3
  startPeriod: 60
```

---

## 10. Rollback Procedure

### Immediate rollback

If a deployment fails:

```bash
# Stop the bad container
docker stop marketplace-backend
docker rm marketplace-backend

# Run the previous version
docker run -d --name marketplace-backend ... marketplace-backend:previous-tag
```

### Database rollback

Flyway does not support automatic rollback. Keep these practices:
- Always backup before migrations
- Write backward-compatible migrations (add columns, don't remove immediately)
- Use a staging environment to test migrations first
- For emergency rollback, restore from RDS snapshot

---

## 11. Zero-Downtime Deployment

Use Blue/Green or Rolling deployment:

1. Start new containers alongside old ones
2. Run health checks on new containers
3. Gradually shift traffic via ALB target groups
4. Remove old containers after traffic is fully shifted

---

## 12. Security Checklist

Before going to production:

- [ ] JWT_SECRET is at least 256-bit random value (not a dictionary word)
- [ ] DELIVERY_CODE_SECRET is unique and strong
- [ ] Database password is strong (20+ chars, symbols)
- [ ] RDS security group only allows EC2 access (no public access)
- [ ] ElastiCache only accessible within VPC
- [ ] Swagger UI disabled in prod (`springdoc.swagger-ui.enabled=false`)
- [ ] Actuator only exposes `/actuator/health` publicly
- [ ] CORS origins are set to actual frontend domains
- [ ] HTTPS is enforced via ALB
- [ ] SSL/TLS on RDS connection enabled
- [ ] Log sensitive data masking verified
- [ ] Stack traces not returned in error responses
