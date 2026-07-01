# Matching API - Sample Requests

Base URL: http://localhost:8080

## Prerequisites

1. Buyer with a PUBLISHED buyer request (Canada/Toronto -> Pakistan/Lahore, Electronics)
2. Traveller with APPROVED KYC and a PUBLISHED trip on the same route
3. Tokens: BUYER_TOKEN, TRAVELLER_TOKEN, ADMIN_TOKEN

---

## Generate matches by buyer request

```bash
curl -X POST http://localhost:8080/api/v1/matches/generate/by-request/BUYER_REQUEST_ID \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Generate matches by traveller trip

```bash
curl -X POST http://localhost:8080/api/v1/matches/generate/by-trip/TRAVELLER_TRIP_ID \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## List matches for buyer request (excludes REJECTED by default)

```bash
curl "http://localhost:8080/api/v1/matches/by-request/BUYER_REQUEST_ID?page=0&size=20&sort=matchScore,desc" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## List matches for traveller trip

```bash
curl "http://localhost:8080/api/v1/matches/by-trip/TRAVELLER_TRIP_ID?page=0&size=20" \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## Get match detail (SUGGESTED -> VIEWED for buyer/traveller)

```bash
curl http://localhost:8080/api/v1/matches/MATCH_ID \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Reject a match

```bash
curl -X POST http://localhost:8080/api/v1/matches/MATCH_ID/reject \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Admin: list all matches

```bash
curl "http://localhost:8080/api/v1/admin/matches?status=SUGGESTED&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Sample response

```json
{
  "success": true,
  "message": "Matches generated",
  "data": [
    {
      "id": "uuid",
      "buyerRequestId": "uuid",
      "buyerRequestTitle": "Electronics from Toronto",
      "buyerId": "uuid",
      "buyerName": "John Buyer",
      "travellerTripId": "uuid",
      "travellerId": "uuid",
      "travellerName": "Jane Traveller",
      "sourceCountry": "Canada",
      "sourceCity": "Toronto",
      "destinationCountry": "Pakistan",
      "destinationCity": "Lahore",
      "itemCategory": "Electronics",
      "neededBefore": "2026-09-01T00:00:00Z",
      "travelDate": "2026-08-20T10:00:00Z",
      "matchScore": 100.00,
      "status": "SUGGESTED",
      "createdAt": "2026-07-01T12:00:00Z",
      "updatedAt": "2026-07-01T12:00:00Z"
    }
  ]
}
```

---

## End-to-end test scenario

1. Register buyer and traveller
2. Traveller submits KYC; admin approves
3. Buyer creates and publishes request (Canada/Toronto -> Pakistan/Lahore, Electronics)
4. Traveller creates and publishes trip (same route, travel before neededBefore)
5. `POST .../generate/by-request/{id}` as buyer -> expect score >= 50
6. `POST .../generate/by-trip/{id}` as traveller -> returns existing match (no duplicate)
7. `GET .../by-request/{id}` -> match appears
8. `GET .../matches/{id}` -> status becomes VIEWED
9. `POST .../matches/{id}/reject` -> status REJECTED
10. `GET .../by-request/{id}` -> rejected match hidden; use `?status=REJECTED` to see it