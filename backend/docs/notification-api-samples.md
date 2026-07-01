# Notification API - Sample Requests

Base URL: http://localhost:8080

## 1. Get my notifications

```bash
curl "http://localhost:8080/api/v1/notifications/my?page=0&size=20" \
  -H "Authorization: Bearer USER_TOKEN"
```

## 2. Get unread count

```bash
curl http://localhost:8080/api/v1/notifications/unread-count \
  -H "Authorization: Bearer USER_TOKEN"
```

## 3. Mark one notification as read

```bash
curl -X POST http://localhost:8080/api/v1/notifications/NOTIFICATION_UUID/read \
  -H "Authorization: Bearer USER_TOKEN"
```

## 4. Mark all notifications as read

```bash
curl -X POST http://localhost:8080/api/v1/notifications/read-all \
  -H "Authorization: Bearer USER_TOKEN"
```

## 5. Admin list notifications

```bash
curl "http://localhost:8080/api/v1/admin/notifications?status=SENT&channel=IN_APP" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 6. Admin send manual notification

```bash
curl -X POST http://localhost:8080/api/v1/admin/notifications/send \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"USER_UUID\",\"channel\":\"IN_APP\",\"notificationType\":\"SYSTEM_ALERT\",\"title\":\"Maintenance notice\",\"message\":\"System maintenance starts at 10 PM.\"}"
```

## Sample notification response

```json
{
  "success": true,
  "data": {
    "id": "notification-uuid",
    "userId": "user-uuid",
    "notificationType": "OFFER_SENT",
    "title": "New offer received",
    "message": "A traveller has sent you an offer.",
    "channel": "IN_APP",
    "status": "SENT",
    "referenceType": "OFFER",
    "referenceId": "offer-uuid",
    "sentAt": "2026-07-01T12:00:00Z",
    "readAt": null,
    "createdAt": "2026-07-01T12:00:00Z",
    "updatedAt": "2026-07-01T12:00:00Z"
  }
}
```

## Expected integration flow

1. Traveller sends offer -> buyer gets `OFFER_SENT`
2. Buyer accepts offer -> traveller gets `OFFER_ACCEPTED`
3. Booking created -> buyer and traveller get `BOOKING_CREATED`
4. Buyer pays -> buyer and traveller get `PAYMENT_HELD`
5. Buyer generates delivery code -> buyer gets `DELIVERY_CODE_GENERATED`
6. Traveller verifies delivery -> buyer and traveller get `DELIVERY_VERIFIED`
7. Payment release -> buyer and traveller get `PAYMENT_RELEASED`
8. Refund flow -> buyer and traveller get `PAYMENT_REFUNDED`
