# Admin & Operations API - Sample Requests

Base URL: http://localhost:8080

Replace `ADMIN_TOKEN`, `USER_UUID`, `BOOKING_UUID`, `PAYMENT_UUID`, `AUDIT_LOG_UUID`, etc.

---

## 1. Admin dashboard summary

```bash
curl http://localhost:8080/api/v1/admin/dashboard/summary \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 2. Admin list users with filters

```bash
curl "http://localhost:8080/api/v1/admin/users?role=BUYER&accountStatus=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 3. Admin user detail

```bash
curl http://localhost:8080/api/v1/admin/users/USER_UUID \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 4. Disable user

```bash
curl -X POST http://localhost:8080/api/v1/admin/users/USER_UUID/disable \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"reason\":\"Policy violation\"}"
```

## 5. Enable user

```bash
curl -X POST http://localhost:8080/api/v1/admin/users/USER_UUID/enable \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 6. Change user role

```bash
curl -X POST http://localhost:8080/api/v1/admin/users/USER_UUID/change-role \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"role\":\"TRAVELLER\"}"
```

## 7. Admin list buyer requests

```bash
curl "http://localhost:8080/api/v1/admin/buyer-requests?status=PUBLISHED&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 8. Admin cancel buyer request

```bash
curl -X POST http://localhost:8080/api/v1/admin/buyer-requests/REQUEST_UUID/cancel \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"reason\":\"Listing violates platform policy\"}"
```

## 9. Admin list traveller trips

```bash
curl "http://localhost:8080/api/v1/admin/traveller-trips?status=PUBLISHED&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 10. Admin cancel traveller trip

```bash
curl -X POST http://localhost:8080/api/v1/admin/traveller-trips/TRIP_UUID/cancel \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"reason\":\"Trip no longer valid\"}"
```

## 11. Admin list bookings

```bash
curl "http://localhost:8080/api/v1/admin/bookings?status=PAYMENT_HELD&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 12. Admin booking detail

```bash
curl http://localhost:8080/api/v1/admin/bookings/BOOKING_UUID \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 13. Admin list payments

```bash
curl "http://localhost:8080/api/v1/admin/payments?status=HELD&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 14. Admin list audit logs

```bash
curl "http://localhost:8080/api/v1/admin/audit-logs?action=ADMIN_USER_DISABLED&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 15. Admin audit log detail

```bash
curl http://localhost:8080/api/v1/admin/audit-logs/AUDIT_LOG_UUID \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Additional admin endpoints

### Dashboard recent activity

```bash
curl "http://localhost:8080/api/v1/admin/dashboard/recent-activity?page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Admin cancel booking

```bash
curl -X POST http://localhost:8080/api/v1/admin/bookings/BOOKING_UUID/cancel \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"reason\":\"Fraud investigation\"}"
```

Returns error if payment is `HELD`: *"Booking has held payment. Refund payment before cancellation."*

### Admin payment release / refund

```bash
curl -X POST http://localhost:8080/api/v1/admin/payments/PAYMENT_UUID/release \
  -H "Authorization: Bearer ADMIN_TOKEN"

curl -X POST http://localhost:8080/api/v1/admin/payments/PAYMENT_UUID/refund \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"reason\":\"Buyer requested refund after dispute\"}"
```

### Admin KYC, disputes, reviews, chat, notifications

Existing module admin routes (all require `ADMIN`):

- `GET /api/v1/admin/kyc/pending`
- `GET /api/v1/admin/kyc`
- `POST /api/v1/admin/kyc/{kycId}/approve`
- `POST /api/v1/admin/kyc/{kycId}/reject`
- `GET /api/v1/admin/disputes`
- `GET /api/v1/admin/disputes/{id}`
- `GET /api/v1/admin/reviews`
- `DELETE /api/v1/admin/reviews/{reviewId}`
- `GET /api/v1/admin/chat/rooms`
- `GET /api/v1/admin/notifications`
- `POST /api/v1/admin/notifications/send`

---

## Testing scenario

1. Login as admin and call dashboard summary.
2. Register buyer and traveller; admin lists users.
3. Admin disables buyer; buyer login/API calls fail with account not active.
4. Admin enables buyer; buyer works again.
5. Admin changes a non-admin user role carefully (not own admin account).
6. Admin views and cancels a listing with reason; check audit logs.
7. Complete booking/payment flow; admin views booking detail and payments.
8. Admin refunds held payment, then cancels booking if needed.
9. Admin reviews disputes, KYC, reviews, and chat history.
10. Confirm audit logs capture `ADMIN_*` actions.

---

## Security notes

- All `/api/v1/admin/**` endpoints require `ADMIN` role.
- Admin cannot disable themselves or change their own role.
- Disabled users are blocked at login and on JWT refresh (`Account is not active`).
- `UserPrincipal.enabled` is false for `DISABLED` accounts, blocking authenticated API access.
