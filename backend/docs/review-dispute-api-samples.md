# Review & Dispute API - Sample Requests

Base URL: http://localhost:8080

Obtain tokens first via auth endpoints (`/api/v1/auth/login`) for buyer, traveller, and admin users.

Replace placeholders:
- `BUYER_TOKEN`, `TRAVELLER_TOKEN`, `ADMIN_TOKEN`
- `BOOKING_UUID`, `USER_UUID`, `DISPUTE_UUID`

---

## Reviews

### 1. Create review as buyer

```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"bookingId\":\"BOOKING_UUID\",\"rating\":5,\"comment\":\"Great delivery, very professional.\"}"
```

### 2. Create review as traveller

```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"bookingId\":\"BOOKING_UUID\",\"rating\":4,\"comment\":\"Buyer was responsive and easy to work with.\"}"
```

### 3. Get user reviews (paginated)

```bash
curl "http://localhost:8080/api/v1/reviews/user/USER_UUID?page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

### 4. Get rating summary

```bash
curl http://localhost:8080/api/v1/reviews/user/USER_UUID/summary \
  -H "Authorization: Bearer BUYER_TOKEN"
```

### Sample review response

```json
{
  "success": true,
  "message": "Review created",
  "data": {
    "id": "review-uuid",
    "bookingId": "booking-uuid",
    "reviewerId": "buyer-uuid",
    "reviewerName": "Jane Buyer",
    "revieweeId": "traveller-uuid",
    "revieweeName": "John Traveller",
    "rating": 5,
    "comment": "Great delivery, very professional.",
    "createdAt": "2026-07-01T12:00:00Z",
    "updatedAt": "2026-07-01T12:00:00Z"
  },
  "timestamp": "2026-07-01T12:00:01Z"
}
```

### Sample rating summary response

```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "averageRating": 4.5,
    "totalReviews": 2,
    "fiveStarCount": 1,
    "fourStarCount": 1,
    "threeStarCount": 0,
    "twoStarCount": 0,
    "oneStarCount": 0
  },
  "timestamp": "2026-07-01T12:00:01Z"
}
```

### Other review endpoints

```bash
# Reviews for a booking (buyer/traveller/admin)
curl http://localhost:8080/api/v1/reviews/booking/BOOKING_UUID \
  -H "Authorization: Bearer BUYER_TOKEN"

# My received reviews
curl "http://localhost:8080/api/v1/reviews/my-received?page=0&size=20" \
  -H "Authorization: Bearer TRAVELLER_TOKEN"

# My given reviews
curl "http://localhost:8080/api/v1/reviews/my-given?page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

---

## Disputes

### 5. Create dispute

```bash
curl -X POST http://localhost:8080/api/v1/disputes \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"bookingId\":\"BOOKING_UUID\",\"reason\":\"DAMAGED_ITEM\",\"description\":\"Item arrived with visible damage to the packaging and product.\"}"
```

### 6. Get my disputes

```bash
curl "http://localhost:8080/api/v1/disputes/my?status=OPEN&page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

### 7. Admin list disputes

```bash
curl "http://localhost:8080/api/v1/admin/disputes?status=OPEN&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### 8. Admin mark dispute under review

```bash
curl -X POST http://localhost:8080/api/v1/admin/disputes/DISPUTE_UUID/mark-under-review \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### 9. Admin resolve dispute

```bash
curl -X POST http://localhost:8080/api/v1/admin/disputes/DISPUTE_UUID/resolve \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"resolutionNote\":\"Refund processed manually after investigation. Case closed.\"}"
```

### 10. Admin reject dispute

```bash
curl -X POST http://localhost:8080/api/v1/admin/disputes/DISPUTE_UUID/reject \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"resolutionNote\":\"Insufficient evidence provided. Dispute rejected.\"}"
```

### Sample dispute response

```json
{
  "success": true,
  "message": "Dispute created",
  "data": {
    "id": "dispute-uuid",
    "bookingId": "booking-uuid",
    "raisedByUserId": "buyer-uuid",
    "raisedByUserName": "Jane Buyer",
    "buyerId": "buyer-uuid",
    "buyerName": "Jane Buyer",
    "travellerId": "traveller-uuid",
    "travellerName": "John Traveller",
    "reason": "DAMAGED_ITEM",
    "description": "Item arrived with visible damage.",
    "status": "OPEN",
    "resolvedByAdminId": null,
    "resolvedByAdminName": null,
    "resolutionNote": null,
    "createdAt": "2026-07-01T13:00:00Z",
    "resolvedAt": null,
    "updatedAt": "2026-07-01T13:00:00Z"
  },
  "timestamp": "2026-07-01T13:00:01Z"
}
```

---

## Dispute reasons

`ITEM_NOT_DELIVERED`, `WRONG_ITEM`, `DAMAGED_ITEM`, `LATE_DELIVERY`, `PAYMENT_ISSUE`, `FRAUD_SUSPICION`, `BEHAVIOUR_ISSUE`, `OTHER`

## Dispute statuses

`OPEN`, `UNDER_REVIEW`, `RESOLVED`, `REJECTED`

---

## Note on dispute notes

`POST /api/v1/disputes/{id}/add-note` is **not implemented** in this module. A separate dispute notes table and endpoints can be added later.

## Optional payment decision endpoints

`POST /api/v1/admin/disputes/{id}/resolve-refund` and `resolve-release` are **not implemented** in this module. Dispute resolution is kept separate from automatic payment refund/release. Admins can still use existing payment admin APIs if needed.

---

## End-to-end testing scenario

### Prerequisites

1. Register/login as **buyer**, **traveller** (with approved KYC), and **admin**.
2. Complete the full booking flow:
   - Buyer publishes request
   - Traveller publishes trip
   - Generate match, send offer, buyer accepts
   - Buyer pays (payment `HELD`, booking `PAYMENT_HELD`)
   - Traveller marks in transit → delivered
   - Buyer generates delivery code, traveller verifies
   - Payment `RELEASED`, booking `COMPLETED`

### Review flow (use completed booking)

3. **Buyer** creates review for traveller (curl #1).
4. **Traveller** creates review for buyer (curl #2).
5. Repeat buyer review → expect `409 CONFLICT` ("already submitted a review").
6. Call rating summary for traveller (curl #4).

### Dispute flow (use a separate booking after payment is held)

7. Start a new booking flow through payment held (or use an in-progress booking).
8. **Buyer or traveller** creates dispute (curl #5).
9. Confirm booking status is `DISPUTED` via `GET /api/v1/bookings/{id}`.
10. **Admin** lists disputes (curl #7), marks under review (curl #8), then resolves (curl #9).
11. Check notifications for buyer and traveller:
    ```bash
    curl "http://localhost:8080/api/v1/notifications/my?page=0&size=20" \
      -H "Authorization: Bearer BUYER_TOKEN"
    ```
12. **Unauthorized user** calling `GET /api/v1/disputes/{id}` should receive `403 FORBIDDEN`.

### Token tips

- Use the `accessToken` from login/register response as the Bearer token.
- Buyer role: `hasRole('BUYER')` for review/dispute create.
- Traveller role: `hasRole('TRAVELLER')`.
- Admin role: required for `/api/v1/admin/disputes/**`.
