# Auth API - Sample Requests

Base URL: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

## Register Buyer

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"buyer@example.com\",\"password\":\"password123\",\"fullName\":\"John Buyer\",\"phoneNumber\":\"+1234567890\",\"role\":\"BUYER\"}"
```

## Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"buyer@example.com\",\"password\":\"password123\"}"
```

## Get Current User

```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

## Refresh Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"REFRESH_TOKEN\"}"
```

## Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"REFRESH_TOKEN\"}"
```
