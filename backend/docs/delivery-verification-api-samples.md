# Delivery Verification API - Sample Requests

Base URL: `http://localhost:8080`

## Prerequisites

- Booking is in `PAYMENT_HELD`
- Buyer has `BUYER_TOKEN`
- Traveller has `TRAVELLER_TOKEN`
- Admin has `ADMIN_TOKEN`

## 1. Buyer generates delivery code

```bash
curl -X POST http://localhost:8080/api/v1/delivery-codes/BOOKING_UUID/generate \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 2. Traveller marks booking in transit

```bash
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-in-transit \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 3. Traveller marks booking delivered pending verification

```bash
curl -X POST http://localhost:8080/api/v1/bookings/BOOKING_UUID/mark-delivered \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 4. Traveller verifies delivery code

```bash
curl -X POST http://localhost:8080/api/v1/delivery-codes/BOOKING_UUID/verify \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"483920\"}"
```

## 5. Buyer or traveller checks code status

```bash
curl http://localhost:8080/api/v1/delivery-codes/BOOKING_UUID/status \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 6. Buyer views payment and sees RELEASED

```bash
curl http://localhost:8080/api/v1/payments/BOOKING_UUID \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 7. Admin expires active code manually

```bash
curl -X POST http://localhost:8080/api/v1/admin/delivery-codes/BOOKING_UUID/expire \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 8. Wrong code

```bash
curl -X POST http://localhost:8080/api/v1/delivery-codes/BOOKING_UUID/verify \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"111111\"}"
```

Response:

```json
{
  "success": false,
  "message": "Invalid delivery verification code",
  "errorCode": "BAD_REQUEST"
}
```

## 9. Reused or expired code

- After a successful verification, the code is `USED` and cannot be reused.
- If the code is older than 10 minutes, verification returns:

```json
{
  "success": false,
  "message": "Delivery verification code has expired",
  "errorCode": "BAD_REQUEST"
}
```

## Sample generate response

```json
{
  "success": true,
  "message": "Delivery verification code generated",
  "data": {
    "id": "code-uuid",
    "bookingId": "booking-uuid",
    "code": "483920",
    "status": "ACTIVE",
    "expiresAt": "2026-07-01T10:10:00Z",
    "createdAt": "2026-07-01T10:00:00Z",
    "message": "Delivery code generated. In production this should be sent only to the buyer via notification."
  }
}
```

## Sample verify response

```json
{
  "success": true,
  "message": "Delivery verified",
  "data": {
    "bookingId": "booking-uuid",
    "paymentId": "payment-uuid",
    "codeStatus": "USED",
    "bookingStatus": "COMPLETED",
    "paymentStatus": "RELEASED",
    "verifiedAt": "2026-07-01T10:05:00Z",
    "paymentReleasedAt": "2026-07-01T10:05:00Z",
    "message": "Delivery verified successfully. Payment released to traveller."
  }
}
```

## End-to-end flow

1. Buyer accepts offer
2. Buyer pays, payment becomes `HELD`
3. Traveller marks booking `IN_TRANSIT`
4. Traveller marks booking `DELIVERED_PENDING_VERIFICATION`
5. Buyer generates code
6. Traveller verifies code
7. Code becomes `USED`
8. Payment becomes `RELEASED`
9. Booking becomes `COMPLETED`
10. Reusing the same code fails
