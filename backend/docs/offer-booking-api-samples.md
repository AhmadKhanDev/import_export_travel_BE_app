# Offer & Booking API - Sample Requests

Base URL: http://localhost:8080

## Prerequisites

- BUYER_TOKEN, TRAVELLER_TOKEN (approved KYC), ADMIN_TOKEN
- Published buyer request and traveller trip with a generated match

---

## Send offer (traveller)

```bash
curl -X POST http://localhost:8080/api/v1/offers \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"buyerRequestId\":\"REQUEST_UUID\",\"travellerTripId\":\"TRIP_UUID\",\"matchId\":\"MATCH_UUID\",\"itemPrice\":500.00,\"travellerFee\":50.00,\"platformFee\":25.00,\"currency\":\"PKR\",\"message\":\"I can deliver on your timeline\",\"expiresAt\":\"2026-08-01T00:00:00Z\"}"
```

## View offers for buyer request

```bash
curl "http://localhost:8080/api/v1/offers/by-request/REQUEST_UUID?status=SENT" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Accept offer (creates booking PAYMENT_PENDING)

```bash
curl -X POST http://localhost:8080/api/v1/offers/OFFER_UUID/accept \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Reject offer

```bash
curl -X POST http://localhost:8080/api/v1/offers/OFFER_UUID/reject \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Cancel offer (traveller)

```bash
curl -X POST http://localhost:8080/api/v1/offers/OFFER_UUID/cancel \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## My bookings

```bash
curl "http://localhost:8080/api/v1/bookings/my?status=PAYMENT_PENDING" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Cancel booking (before PAYMENT_HELD)

```bash
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/cancel \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## Mark in transit (requires PAYMENT_HELD - payment module next)

```bash
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-in-transit \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## Mark delivered

```bash
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-delivered \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## Admin: list offers

```bash
curl "http://localhost:8080/api/v1/admin/offers?status=SENT" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## Admin: list bookings

```bash
curl "http://localhost:8080/api/v1/admin/bookings?status=PAYMENT_PENDING" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Sample accept response (booking created)

```json
{
  "success": true,
  "message": "Offer accepted, booking created",
  "data": {
    "id": "booking-uuid",
    "offerId": "offer-uuid",
    "buyerId": "buyer-uuid",
    "buyerName": "John Buyer",
    "travellerId": "traveller-uuid",
    "travellerName": "Jane Traveller",
    "buyerRequestId": "request-uuid",
    "buyerRequestTitle": "Electronics from Toronto",
    "travellerTripId": "trip-uuid",
    "sourceCountry": "Canada",
    "sourceCity": "Toronto",
    "destinationCountry": "Pakistan",
    "destinationCity": "Lahore",
    "itemPrice": 500.00,
    "travellerFee": 50.00,
    "platformFee": 25.00,
    "totalAmount": 575.00,
    "currency": "PKR",
    "status": "PAYMENT_PENDING",
    "acceptedAt": "2026-07-01T12:00:00Z",
    "createdAt": "2026-07-01T12:00:00Z"
  }
}
```

---

## End-to-end test flow

1. Buyer publishes request; traveller publishes trip (approved KYC)
2. Generate match via matching module
3. Traveller sends offer with matchId
4. Buyer views offers: `GET /offers/by-request/{id}`
5. Buyer accepts: booking `PAYMENT_PENDING`, offer `ACCEPTED`, request `BOOKED`, trip `MATCHED`
6. Other SENT offers for same request auto-rejected
7. Test reject/cancel on separate offers
8. Test wrong user token returns 403