# Chat API - Sample Requests

Base URL: http://localhost:8080

Replace placeholders:
- `BUYER_TOKEN`, `TRAVELLER_TOKEN`, `ADMIN_TOKEN`
- `BOOKING_UUID`, `ROOM_UUID`

---

## 1. Create or get chat room by bookingId

```bash
curl -X POST http://localhost:8080/api/v1/chat/rooms/BOOKING_UUID \
  -H "Authorization: Bearer BUYER_TOKEN"
```

When a booking is accepted, a chat room is also created automatically with a system message. This endpoint returns the existing room or creates one if missing.

## 2. Get my chat rooms

```bash
curl "http://localhost:8080/api/v1/chat/rooms/my?status=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer BUYER_TOKEN"
```

## 3. Send text message

```bash
curl -X POST http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/messages \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Hi, can we meet at the airport arrival hall?\",\"messageType\":\"TEXT\"}"
```

## 4. Send image message using attachmentUrl

```bash
curl -X POST http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/messages \
  -H "Authorization: Bearer TRAVELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Photo of the item\",\"messageType\":\"IMAGE\",\"attachmentUrl\":\"https://cdn.example.com/uploads/item-photo.jpg\"}"
```

## 5. Get messages

```bash
# Oldest first (default ASC)
curl "http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/messages?page=0&size=50&sortDirection=ASC" \
  -H "Authorization: Bearer BUYER_TOKEN"

# Newest first
curl "http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/messages?page=0&size=50&sortDirection=DESC" \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 6. Mark messages as read

```bash
curl -X POST http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/read \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 7. Get unread count

```bash
curl http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/unread-count \
  -H "Authorization: Bearer TRAVELLER_TOKEN"
```

## 8. Admin list chat rooms

```bash
curl "http://localhost:8080/api/v1/admin/chat/rooms?status=ACTIVE&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 9. Admin view messages

```bash
curl "http://localhost:8080/api/v1/admin/chat/rooms/ROOM_UUID/messages?page=0&size=50" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 10. Admin close chat room

```bash
curl -X POST http://localhost:8080/api/v1/chat/rooms/ROOM_UUID/close \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Sample chat room response

```json
{
  "success": true,
  "message": "Chat room ready",
  "data": {
    "id": "room-uuid",
    "bookingId": "booking-uuid",
    "buyerId": "buyer-uuid",
    "buyerName": "Jane Buyer",
    "travellerId": "traveller-uuid",
    "travellerName": "John Traveller",
    "status": "ACTIVE",
    "lastMessage": "Booking created. You can now discuss delivery details here.",
    "lastMessageAt": "2026-07-01T10:00:00Z",
    "unreadCount": 1,
    "createdAt": "2026-07-01T10:00:00Z",
    "updatedAt": "2026-07-01T10:00:00Z"
  },
  "timestamp": "2026-07-01T10:00:01Z"
}
```

## Sample message response

```json
{
  "success": true,
  "message": "Message sent",
  "data": {
    "id": "message-uuid",
    "chatRoomId": "room-uuid",
    "senderId": "buyer-uuid",
    "senderName": "Jane Buyer",
    "message": "Hi, can we meet at the airport arrival hall?",
    "messageType": "TEXT",
    "attachmentUrl": null,
    "sentAt": "2026-07-01T10:05:00Z",
    "readAt": null,
    "createdAt": "2026-07-01T10:05:00Z"
  },
  "timestamp": "2026-07-01T10:05:01Z"
}
```

## Sample mark-read response

```json
{
  "success": true,
  "message": "Messages marked as read",
  "data": {
    "updatedCount": 3
  },
  "timestamp": "2026-07-01T10:10:01Z"
}
```

---

## Automatic system messages

The platform adds system messages when:
- Booking is created (offer accepted)
- Payment is held in escrow
- Delivery is verified and payment released
- A dispute is created

Users cannot send `SYSTEM` messages via the API.

---

## End-to-end testing flow

1. Complete flow until booking is created (offer accepted).
2. Buyer calls `POST /api/v1/chat/rooms/{bookingId}` or uses `GET /api/v1/chat/rooms/my` to get `ROOM_UUID`.
3. Traveller opens the same room via `/my` or create/get endpoint.
4. Buyer sends a text message (curl #3).
5. Traveller checks unread count (curl #7) — should be > 0.
6. Traveller marks messages as read (curl #6).
7. Unread count becomes 0.
8. Traveller sends a reply.
9. Buyer checks notifications: `GET /api/v1/notifications/my`.
10. Admin views messages (curl #9).
11. Admin closes chat room (curl #10).
12. Buyer/traveller attempt to send a message — expect `400` with "Chat room is closed".

---

## Notes

- REST polling is used for now. WebSocket real-time messaging can be added later.
- `attachmentUrl` must be a valid `http` or `https` URL. File upload storage is not implemented yet.
- One chat room per booking (`booking_id` is unique).
- Chat remains readable after booking `COMPLETED` or `CANCELLED` unless the room is `CLOSED`.
