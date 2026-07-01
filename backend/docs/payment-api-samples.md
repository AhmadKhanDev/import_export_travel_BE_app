# Payment & Escrow API - Sample Requests

Base URL: http://localhost:8080

## Prerequisites

- Booking in PAYMENT_PENDING status (after buyer accepts offer)
- BUYER_TOKEN, TRAVELLER_TOKEN, ADMIN_TOKEN

---

## 1. Buyer pays for booking

```bash
curl -X POST http://localhost:8080/api/v1/payments/BOOKING_UUID/pay \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Idempotency-Key: pay-booking-001" \
  -H "Content-Type: application/json" \
  -d "{\"paymentProvider\":\"FAKE\",\"note\":\"Escrow payment\"}"
```

## 2. Repeat with same Idempotency-Key (returns existing payment)

```bash
curl -X POST http://localhost:8080/api/v1/payments/BOOKING_UUID/pay \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Idempotency-Key: pay-booking-001" \
  -H "Content-Type: application/json" \
  -d "{}"
```

## 3. Buyer views payment by booking ID

```bash
curl http://localhost:8080/api/v1/payments/BOOKING_UUID \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 4. Traveller views payment

```bash
curl http://localhost:8080/api/v1/payments/BOOKING_UUID \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 5. My payments

```bash
curl "http://localhost:8080/api/v1/payments/my?status=HELD" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 6. Admin lists payments

```bash
curl "http://localhost:8080/api/v1/admin/payments?status=HELD" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 7. Mark booking delivered (traveller) then admin releases payment

```bash
# First mark in transit (after PAYMENT_HELD)
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-in-transit \
  -H "Authorization: Bearer TRAVELLER_TOKEN"

# Mark delivered
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-delivered \
  -H "Authorization: Bearer TRAVELLER_TOKEN"

# Admin release (before delivery -> error)
curl -X POST http://localhost:8080/api/v1/payments/PAYMENT_UUID/release \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 8. Admin refund

```bash
curl -X POST http://localhost:8080/api/v1/payments/PAYMENT_UUID/refund \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Sample pay response

```json
{
  "success": true,
  "message": "Payment successful, funds held in escrow",
  "data": {
    "id": "payment-uuid",
    "bookingId": "booking-uuid",
    "buyerId": "buyer-uuid",
    "buyerName": "John Buyer",
    "travellerId": "traveller-uuid",
    "travellerName": "Jane Traveller",
    "amount": 575.00,
    "platformFee": 25.00,
    "travellerPayout": 550.00,
    "currency": "PKR",
    "paymentProvider": "FAKE",
    "providerPaymentId": "FAKE-PAY-uuid",
    "status": "HELD",
    "paidAt": "2026-07-01T12:00:00Z",
    "createdAt": "2026-07-01T12:00:00Z"
  }
}
```

---

## End-to-end flow

1. Accept offer -> booking PAYMENT_PENDING
2. Buyer pays -> payment HELD, booking PAYMENT_HELD
3. Admin release before delivery -> 400 error
4. Traveller mark-in-transit -> IN_TRANSIT
5. Traveller mark-delivered -> DELIVERED_PENDING_VERIFICATION
6. Admin release -> payment RELEASED, booking COMPLETED
7. Separate booking: admin refund before completion -> payment REFUNDED, booking CANCELLED