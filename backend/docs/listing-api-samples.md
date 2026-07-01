# Listing API - Sample Requests

Base URL: http://localhost:8080

## Setup tokens

Register/login as buyer and traveller first. Save access tokens:

- BUYER_TOKEN from POST /api/v1/auth/login (buyer account)
- TRAVELLER_TOKEN from POST /api/v1/auth/login (traveller account)

---

## Buyer Requests

### Create buyer request (DRAFT)

```bash
curl -X POST http://localhost:8080/api/v1/buyer-requests \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"iPhone 15 Pro\",\"description\":\"Space black 256GB\",\"itemCategory\":\"Electronics\",\"brand\":\"Apple\",\"sourceCountry\":\"USA\",\"sourceCity\":\"New York\",\"destinationCountry\":\"UK\",\"destinationCity\":\"London\",\"estimatedItemPrice\":999.99,\"neededBefore\":\"2026-08-15T00:00:00Z\"}"
```

### Publish buyer request

```bash
curl -X POST http://localhost:8080/api/v1/buyer-requests/{id}/publish \
  -H "Authorization: Bearer BUYER_TOKEN"
```

### List published buyer requests (public search)

```bash
curl "http://localhost:8080/api/v1/buyer-requests?sourceCountry=USA&destinationCountry=UK&itemCategory=Electronics&page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

### My buyer requests

```bash
curl "http://localhost:8080/api/v1/buyer-requests/my?page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

---

## Traveller Trips

### Create traveller trip (DRAFT)

```bash
curl -X POST http://localhost:8080/api/v1/traveller-trips \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sourceCountry\":\"USA\",\"sourceCity\":\"New York\",\"destinationCountry\":\"UK\",\"destinationCity\":\"London\",\"travelDate\":\"2026-08-10T10:00:00Z\",\"availableCapacityKg\":5.0,\"allowedItemTypes\":\"Electronics,Clothing\"}"
```

### Publish traveller trip (requires APPROVED KYC)

```bash
curl -X POST http://localhost:8080/api/v1/traveller-trips/{id}/publish \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

### Search published trips

```bash
curl "http://localhost:8080/api/v1/traveller-trips?sourceCountry=USA&destinationCountry=UK&travelDateFrom=2026-08-01T00:00:00Z" \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

---

## Sample response (buyer request)

```json
{
  "success": true,
  "message": "Buyer request created",
  "data": {
    "id": "uuid",
    "buyerId": "uuid",
    "buyerName": "John Buyer",
    "title": "iPhone 15 Pro",
    "description": "Space black 256GB",
    "itemCategory": "Electronics",
    "brand": "Apple",
    "sourceCountry": "USA",
    "sourceCity": "New York",
    "destinationCountry": "UK",
    "destinationCity": "London",
    "estimatedItemPrice": 999.99,
    "status": "DRAFT",
    "neededBefore": "2026-08-15T00:00:00Z",
    "createdAt": "2026-07-01T12:00:00Z",
    "updatedAt": "2026-07-01T12:00:00Z"
  }
}
```

## Testing flow

1. Login as BUYER, create + publish a buyer request
2. Login as TRAVELLER with approved KYC, create + publish a matching trip
3. Search listings without status filter (only PUBLISHED shown for non-admin)
4. Admin can pass status=DRAFT to see drafts