# Traveller-Buyer Marketplace — End-to-End Flow & Testing Guide

**Version:** 1.0  
**Date:** July 2026  
**Prepared for:** Ahmad Khan  
**Frontend:** http://localhost:5174  
**Backend:** http://localhost:8080  
**Swagger UI:** http://localhost:8080/swagger-ui/index.html

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Roles in the System](#2-roles-in-the-system)
3. [Before You Start Testing](#3-before-you-start-testing)
4. [Recommended Test Users](#4-recommended-test-users)
5. [Full End-to-End Happy Path Summary](#5-full-end-to-end-happy-path-summary)
6. [Auth Flow Testing](#6-auth-flow-testing)
7. [Profile Flow Testing](#7-profile-flow-testing)
8. [Traveller KYC Testing](#8-traveller-kyc-testing)
9. [Buyer Request Testing](#9-buyer-request-testing)
10. [Traveller Trip Testing](#10-traveller-trip-testing)
11. [Matching Testing](#11-matching-testing)
12. [Offer Testing](#12-offer-testing)
13. [Booking Testing](#13-booking-testing)
14. [Payment & Escrow Testing](#14-payment--escrow-testing)
15. [Delivery Verification Testing](#15-delivery-verification-testing)
16. [Notification Testing](#16-notification-testing)
17. [Chat Testing](#17-chat-testing)
18. [Review Testing](#18-review-testing)
19. [Dispute Testing](#19-dispute-testing)
20. [Admin Panel Testing](#20-admin-panel-testing)
21. [Complete Status Flow Reference](#21-complete-status-flow-reference)
22. [API Testing via Browser DevTools](#22-api-testing-via-browser-devtools)
23. [Database Verification](#23-database-verification)
24. [End-to-End Testing Checklist](#24-end-to-end-testing-checklist)
25. [Common Errors and Fixes](#25-common-errors-and-fixes)
26. [Test Report Template](#26-test-report-template)
27. [Final Notes](#27-final-notes)

---

## 1. Project Overview

The Traveller-Buyer Marketplace connects two types of users:

- **Buyer**: Needs an item from another city or country but cannot travel there personally.
- **Traveller**: Is already travelling through the item's source location and can carry it.

### How it works

1. A Buyer posts a request saying "I want this item from Toronto to Lahore."
2. A Traveller posts a trip saying "I am flying from Toronto to Lahore on this date."
3. The system matches compatible requests and trips based on route, date, and category.
4. The Traveller sends an offer to the Buyer.
5. The Buyer accepts the offer — this creates a Booking.
6. The Buyer pays — the money is held safely in escrow (not released yet).
7. The Buyer generates a 6-digit delivery verification code.
8. The Traveller delivers the item and enters the code to confirm delivery.
9. The system verifies the code and releases the payment to the Traveller.
10. Both users can review each other.
11. If anything goes wrong, either party can raise a Dispute for Admin to resolve.

### Full Flow Diagram

```
[Register / Login]
        ↓
[Complete Profile]
        ↓
[KYC Verification] (Traveller only)
        ↓
[Buyer: Create & Publish Request] ←→ [Traveller: Create & Publish Trip]
        ↓
[System: Generate Matches]
        ↓
[Traveller: Send Offer]
        ↓
[Buyer: Accept Offer → Booking Created]
        ↓
[Buyer: Pay → Payment HELD in Escrow]
        ↓
[Buyer: Generate Delivery Code]
        ↓
[Traveller: Mark In Transit → Mark Delivered → Verify Code]
        ↓
[System: Payment RELEASED to Traveller]
        ↓
[Both: Leave Reviews]
        ↓
[Dispute if needed → Admin Resolves]
```

---

## 2. Roles in the System

| Role | What they can do |
|------|-----------------|
| **BUYER** | Register, login, complete profile, create/publish buyer requests, view matches, view/accept/reject offers, pay for bookings, generate delivery code, chat, review travellers, create disputes, view notifications |
| **TRAVELLER** | Register, login, complete profile, submit KYC, create/publish trips (after KYC approval), view matches, send offers, view bookings, mark in-transit/delivered, verify delivery code, chat, review buyers, create disputes, view notifications |
| **ADMIN** | Login, view dashboard, approve/reject KYC, manage users (enable/disable/change role), view all listings/bookings/payments, release/refund payments, resolve disputes, view audit logs, send manual notifications |

---

## 3. Before You Start Testing

### Services that must be running

| Service | How to start | How to check |
|---------|--------------|--------------|
| Docker (Postgres + Redis) | `docker compose up -d postgres redis` | `docker compose ps` |
| Backend (Spring Boot) | `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` | `http://localhost:8080/actuator/health` |
| Frontend (Vite) | `npm run dev` | `http://localhost:5174` |

> Run all commands from the `backend/` folder for Maven, and from `traveller-marketplace-frontend/` for npm.

### Verify each service

**1. Docker containers:**
```
docker compose ps
```
Expected output: `marketplace-postgres` and `marketplace-redis` both showing `running`.

**2. Backend health:**

Open in browser: `http://localhost:8080/actuator/health`

Expected:
```json
{ "status": "UP" }
```

**3. Swagger UI:**

Open: `http://localhost:8080/swagger-ui/index.html`

Expected: Swagger page loads with all API groups visible.

**4. Frontend:**

Open: `http://localhost:5174`

Expected: Login page appears. No CORS errors in browser console (F12 → Console tab).

### CORS note

The frontend runs on port **5174**. If you see `CORS error` in the browser console, the backend's `application-local.yml` must include `http://localhost:5174` in `app.cors.allowed-origins`. This is already configured in this project.

---

## 4. Recommended Test Users

You need to create these users before testing. The system does not seed them automatically.

| Role | Email | Password | How to create |
|------|-------|----------|---------------|
| Admin | admin@test.com | Admin@123 | Register as BUYER, then update role in DB (see below) |
| Buyer | buyer@test.com | Buyer@123 | Register via frontend Register page, select BUYER |
| Traveller | traveller@test.com | Traveller@123 | Register via frontend Register page, select TRAVELLER |
| Buyer 2 (optional) | buyer2@test.com | Buyer@123 | For negative testing |
| Traveller 2 (optional) | traveller2@test.com | Traveller@123 | For negative testing |

### How to create the Admin user

The API does not allow direct registration as ADMIN (it is blocked by design).

**Step 1:** Register normally via the frontend Register page with email `admin@test.com`, password `Admin@123`, and role **BUYER**.

**Step 2:** Open a database shell:
```bash
docker exec -it marketplace-postgres psql -U marketplace -d marketplace
```

**Step 3:** Update the role:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@test.com';
```

**Step 4:** Verify:
```sql
SELECT id, email, role FROM users WHERE email = 'admin@test.com';
```

**Step 5:** Now login with `admin@test.com` / `Admin@123` on the frontend. You will be redirected to the Admin Dashboard.

### Password rules

Passwords must match the backend's validation. Based on test accounts used in the backend setup guide, use a password with uppercase, lowercase, digit, and special character, e.g.: `Buyer@123`.

---

## 5. Full End-to-End Happy Path Summary

This table shows the complete journey from start to finish using one Buyer and one Traveller.

| Step | Role | Frontend Screen | Action | Backend API | Expected Result |
|------|------|-----------------|--------|-------------|-----------------|
| 1 | Buyer | Register Page (`/register`) | Register as buyer@test.com | `POST /api/v1/auth/register` | Account created, redirected to Buyer Dashboard |
| 2 | Traveller | Register Page (`/register`) | Register as traveller@test.com | `POST /api/v1/auth/register` | Account created, redirected to Traveller Dashboard |
| 3 | Admin | DB shell | UPDATE users SET role='ADMIN' | (direct DB) | admin@test.com is now ADMIN |
| 4 | Traveller | KYC Page (`/traveller/kyc`) | Submit KYC documents | `POST /kyc/submit` ⚠️ | KYC status → PENDING_REVIEW |
| 5 | Admin | Admin KYC Page (`/admin/kyc`) | Approve traveller's KYC | `POST /api/v1/admin/kyc/{id}/approve` | KYC status → APPROVED |
| 6 | Buyer | Create Buyer Request (`/buyer/requests/create`) | Create request: iPhone 15 Pro Case | `POST /api/v1/buyer-requests` | Request saved as DRAFT |
| 7 | Buyer | My Requests (`/buyer/requests`) | Click Publish on the draft request | `POST /api/v1/buyer-requests/{id}/publish` | Status → PUBLISHED |
| 8 | Traveller | Create Trip (`/traveller/trips/create`) | Create trip: Toronto → Lahore | `POST /api/v1/traveller-trips` | Trip saved as DRAFT |
| 9 | Traveller | My Trips (`/traveller/trips`) | Click Publish on the draft trip | `POST /api/v1/traveller-trips/{id}/publish` | Status → PUBLISHED |
| 10 | Buyer | Buyer Request Detail (`/buyer/requests/:id`) | Click Generate Matches | `POST /api/v1/matches/generate/by-request/{id}` | Matches created, sorted by score |
| 11 | Traveller | Traveller Matches (`/traveller/matches`) | View matching buyer requests | `GET /api/v1/matches/by-trip/{id}` | Buyer's request appears |
| 12 | Traveller | Traveller Trip Detail (`/traveller/trips/:id`) | Click Send Offer | `POST /api/v1/offers` | Offer status → SENT |
| 13 | Buyer | Buyer Offers (`/buyer/offers`) | View offer, click Accept | `POST /api/v1/offers/{id}/accept` | Booking created, status → PAYMENT_PENDING |
| 14 | Buyer | Booking Detail (`/shared/bookings/:id`) | Click Pay | `POST /api/v1/payments/{bookingId}/pay` | Payment status → HELD, Booking → PAYMENT_HELD |
| 15 | Buyer | Delivery Verification (`/shared/delivery/:id`) | Click Generate Code | `POST /api/v1/delivery-codes/{bookingId}/generate` | 6-digit code shown to buyer |
| 16 | Traveller | Traveller Bookings (`/traveller/bookings`) | Click Mark In Transit | `POST /api/v1/bookings/{id}/mark-in-transit` | Booking → IN_TRANSIT |
| 17 | Traveller | Traveller Bookings | Click Mark Delivered | `POST /api/v1/bookings/{id}/mark-delivered` | Booking → DELIVERED_PENDING_VERIFICATION |
| 18 | Traveller | Delivery Verification (`/shared/delivery/:id`) | Enter the buyer's 6-digit code | `POST /api/v1/delivery-codes/{bookingId}/verify` | Code verified, Booking → COMPLETED, Payment → RELEASED |
| 19 | Buyer | Reviews (`/shared/reviews`) | Submit 5-star review for traveller | `POST /api/v1/reviews` | Review saved |
| 20 | Traveller | Reviews (`/shared/reviews`) | Submit 5-star review for buyer | `POST /api/v1/reviews` | Review saved |

> ⚠️ **Note on Step 4 (KYC):** The backend does not currently have a user-facing `/kyc/submit` endpoint — only admin KYC endpoints exist. If the KYC page returns 404, you can use the database directly to create a KYC record and then approve it from the Admin panel. See [Section 8](#8-traveller-kyc-testing) for details.

---

## 6. Auth Flow Testing

### Frontend screens involved

| Screen | URL |
|--------|-----|
| Login Page | `/login` |
| Register Page | `/register` |

### Test Data

**Buyer registration:**
```
Full Name: Test Buyer
Email: buyer@test.com
Phone: +447700900001
Password: Buyer@123
Role: BUYER (select from dropdown)
```

**Traveller registration:**
```
Full Name: Test Traveller
Email: traveller@test.com
Phone: +447700900002
Password: Traveller@123
Role: TRAVELLER (select from dropdown)
```

### Step-by-step testing

**Test 1: Register a Buyer**

1. Open `http://localhost:5174`
2. Click "Create one" (register link)
3. Fill the form with buyer test data above
4. Click **Sign Up**

**Frontend action:** Form submits via `registerApi.register()`  
**API called:** `POST http://localhost:8080/api/v1/auth/register`  
**Request body:**
```json
{
  "email": "buyer@test.com",
  "password": "Buyer@123",
  "fullName": "Test Buyer",
  "phoneNumber": "+447700900001",
  "role": "BUYER"
}
```
**Expected response:**
```json
{
  "success": true,
  "message": "...",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "...",
    "user": { "id": "...", "email": "buyer@test.com", "role": "BUYER" }
  }
}
```
**Expected result:**  
- Tokens saved to `localStorage` with keys `tm_access_token` and `tm_refresh_token`
- User redirected to **Buyer Dashboard** (`/buyer/dashboard`)
- Sidebar shows Buyer navigation items

**Test 2: Register a Traveller**

Same steps, use traveller test data above, select **TRAVELLER** role.  
**Expected result:** Redirected to **Traveller Dashboard** (`/traveller/dashboard`)

**Test 3: Login**

1. Go to `/login`
2. Enter `buyer@test.com` / `Buyer@123`
3. Click **Sign In**

**API called:** `POST /api/v1/auth/login`  
**Expected result:** Redirected to `/buyer/dashboard`

**Test 4: Token refresh (automatic)**

The Axios client automatically refreshes the token when a 401 is received. You do not need to test this manually, but it happens transparently when the 15-minute access token expires.

**Token refresh API:** `POST /api/v1/auth/refresh`  
**Request body:** `{ "refreshToken": "..." }`

**Test 5: Logout**

Click the **Logout** button at the bottom of the sidebar.  
**API called:** `POST /api/v1/auth/logout` (body: `{ refreshToken }`)  
**Expected result:** Tokens cleared from `localStorage`, redirected to `/login`

**Test 6: Get current user info**

After login, the frontend calls this automatically:  
**API called:** `GET /api/v1/auth/me`  
**Expected response:** `{ "data": { "id": "...", "email": "...", "role": "BUYER", ... } }`

### Negative tests for Auth

| Test | Steps | Expected Error |
|------|-------|---------------|
| Wrong password | Login with buyer@test.com / wrongpass | 401 Unauthorized — "Invalid credentials" |
| Duplicate email | Register with buyer@test.com again | 409 Conflict — "Email already in use" |
| Empty email | Submit login form with blank email | 400 — form validation error shown |
| Invalid email format | Enter "notanemail" as email | 400 — form validation error |
| Access dashboard without login | Open `/buyer/dashboard` directly in new browser | Redirected to `/login` |
| Buyer accesses traveller route | Login as buyer, navigate to `/traveller/dashboard` | Redirected to `/buyer/dashboard` |
| Traveller accesses admin route | Login as traveller, navigate to `/admin/dashboard` | Redirected to `/traveller/dashboard` |

---

## 7. Profile Flow Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Profile Page | `/buyer/profile` | BUYER |
| Profile Page | `/traveller/profile` | TRAVELLER |
| Profile Page | `/admin/profile` (via Topbar) | ADMIN |

> ⚠️ **Backend Note:** The frontend `profileApi.ts` calls `GET /api/v1/profile/me` and `PUT /api/v1/profile/me`. These endpoints do **not** currently exist in the backend — there is no `ProfileController`. If you see 404, this feature is not yet backend-wired. The `GET /api/v1/auth/me` endpoint returns basic user info (id, email, fullName, role) but not full profile data.

### Step-by-step testing (if backend profile endpoints are added)

**Test 1: View Profile**

1. Login as `buyer@test.com`
2. Click **Profile** in the sidebar (or navigate to `/buyer/profile`)

**API called:** `GET /api/v1/profile/me`  
**Expected result:** Profile form loaded with existing data

**Test 2: Update Profile**

1. On Profile page, fill:
   - Full Name: Test Buyer
   - Bio: I am a buyer from London
   - City: London
   - Country: United Kingdom
2. Click **Save Changes**

**API called:** `PUT /api/v1/profile/me`  
**Expected result:** Profile saved, success message shown

### Negative tests for Profile

| Test | Expected |
|------|----------|
| Submit with empty country | 400 validation error |
| Unauthenticated user opens profile URL | Redirected to `/login` |

---

## 8. Traveller KYC Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| KYC Page | `/traveller/kyc` | TRAVELLER |
| Admin KYC Page | `/admin/kyc` | ADMIN |

### How KYC works

1. Traveller submits KYC documents → status becomes `PENDING_REVIEW`
2. Admin reviews and either approves or rejects
3. If **APPROVED**, traveller can create and publish trips
4. If **REJECTED**, traveller must resubmit with a reason

### Backend KYC Status values

`NOT_SUBMITTED` → `PENDING_REVIEW` → `APPROVED`  
`PENDING_REVIEW` → `REJECTED` (admin can reject)

### Step-by-step: Traveller submits KYC

> ⚠️ **Important:** The frontend KYC form calls `POST /api/v1/kyc/submit` and `GET /api/v1/kyc/my`. The backend currently only exposes KYC endpoints under `/api/v1/admin/kyc/`. If the frontend form returns 404, use the database workaround below.

**Option A — Frontend (if endpoint is available):**

1. Login as `traveller@test.com`
2. Click **KYC** in the sidebar → go to `/traveller/kyc`
3. Fill the form:
   - Document Type: PASSPORT
   - Document Number: AB1234567
   - Document URL: https://example.com/passport.jpg
4. Click **Submit KYC**

**API called:** `POST /api/v1/kyc/submit`  
**Expected result:** KYC status shows `PENDING_REVIEW`

**Option B — Database workaround (if frontend KYC form returns 404):**

```sql
-- Connect to DB first:
docker exec -it marketplace-postgres psql -U marketplace -d marketplace

-- Get the traveller's user ID:
SELECT id FROM users WHERE email = 'traveller@test.com';

-- Insert a KYC record with PENDING_REVIEW status:
INSERT INTO kyc_documents (id, user_id, document_type, document_number, status, created_at, updated_at)
VALUES (gen_random_uuid(), '<traveller-user-id>', 'PASSPORT', 'AB1234567', 'PENDING_REVIEW', NOW(), NOW());
```

### Step-by-step: Admin approves KYC

1. Login as `admin@test.com`
2. Click **KYC** in the Admin sidebar → go to `/admin/kyc`
3. See the pending KYC submission from the traveller
4. Click **Approve**

**API called:** `POST /api/v1/admin/kyc/{kycId}/approve`  
**Expected result:**
- KYC status → `APPROVED`
- Traveller receives `KYC_APPROVED` notification
- Audit log created: `ADMIN_KYC_APPROVED`

### Step-by-step: Admin rejects KYC

1. On Admin KYC page, click **Reject** on a pending submission
2. Enter rejection reason: "Document is blurry, please resubmit"
3. Confirm

**API called:** `POST /api/v1/admin/kyc/{kycId}/reject`  
**Request body:** `{ "reason": "Document is blurry, please resubmit" }`  
**Expected result:**
- KYC status → `REJECTED`
- Traveller receives `KYC_REJECTED` notification with reason

### Negative tests for KYC

| Test | Expected |
|------|----------|
| Buyer navigates to `/traveller/kyc` | Redirected to `/buyer/dashboard` (role guard) |
| Traveller tries to create trip before KYC approval | 400 — "Traveller must have approved KYC before this action" |
| Non-admin accesses `/api/v1/admin/kyc/*` directly | 403 Forbidden |
| Reject KYC without a reason | 400 validation error |

---

## 9. Buyer Request Testing

### Frontend screens involved

| Screen | URL |
|--------|-----|
| Buyer Dashboard | `/buyer/dashboard` |
| Create Buyer Request | `/buyer/requests/create` |
| My Buyer Requests | `/buyer/requests` |
| Buyer Request Detail | `/buyer/requests/:id` |

### Test data

```
Title: iPhone 15 Pro Case from Canada
Description: I need a good quality silicone phone case from Toronto, Canada.
Item Category: Electronics
Estimated Item Price: 5000
Currency: PKR
Traveller's Reward: 2500
Source Country: Canada
Source City: Toronto
Destination Country: Pakistan
Destination City: Lahore
Deadline Date: 2026-09-01
```

### Step-by-step testing

**Test 1: Create a buyer request (save as Draft)**

1. Login as `buyer@test.com`
2. Click **Create Request** in sidebar → go to `/buyer/requests/create`
3. Fill the form with test data above
4. Click **Save as Draft** (or equivalent submit button)

**API called:** `POST /api/v1/buyer-requests`  
**Expected result:**
- Request created with status `DRAFT`
- Redirected to request detail or list page

**Test 2: View my requests**

1. Click **My Requests** in sidebar → go to `/buyer/requests`
2. The new draft request should appear in the table

**API called:** `GET /api/v1/buyer-requests/my`

**Test 3: Publish the request**

1. On My Requests page, find the draft request
2. Click **Publish**

**API called:** `POST /api/v1/buyer-requests/{id}/publish`  
**Expected result:** Status changes from `DRAFT` to `PUBLISHED`

**Test 4: View request detail**

1. Click on the request row to open detail → `/buyer/requests/:id`

**API called:** `GET /api/v1/buyer-requests/{id}`  
**Expected result:** Full request details shown

**Test 5: Cancel a published request**

1. On the request detail page, click **Cancel**
2. Confirm the action

**API called:** `POST /api/v1/buyer-requests/{id}/cancel`  
**Expected result:** Status → `CANCELLED`

**Test 6: Delete a draft request**

1. From My Requests, click **Delete** on a DRAFT request

**API called:** `DELETE /api/v1/buyer-requests/{id}`  
**Expected result:** Request removed from list

### Buyer request status transitions

```
DRAFT → PUBLISHED (via publish)
PUBLISHED → CANCELLED (via cancel)
PUBLISHED → BOOKED (after an offer is accepted)
BOOKED → COMPLETED (after full booking flow completes)
```

### Negative tests for Buyer Requests

| Test | Expected |
|------|----------|
| Traveller navigates to `/buyer/requests/create` | Redirected to traveller dashboard (role guard) |
| Buyer calls `POST /api/v1/buyer-requests` with missing title | 400 validation error |
| Buyer tries to publish request owned by another user | 403 Forbidden |
| Buyer tries to delete a PUBLISHED request | 400 — cannot delete non-draft |
| Deadline date in the past | 400 validation error |

---

## 10. Traveller Trip Testing

### Frontend screens involved

| Screen | URL |
|--------|-----|
| Traveller Dashboard | `/traveller/dashboard` |
| Create Trip | `/traveller/trips/create` |
| My Trips | `/traveller/trips` |
| Trip Detail | `/traveller/trips/:id` |

### Test data

```
Source Country: Canada
Source City: Toronto
Destination Country: Pakistan
Destination City: Lahore
Travel Date: 2026-08-15
Return Date: 2026-08-30 (optional)
Available Capacity (kg): 5
Notes: I can carry electronics and small items
```

### Step-by-step testing

**Pre-condition:** Traveller's KYC must be `APPROVED` before creating a trip.

**Test 1: Create a trip (save as Draft)**

1. Login as `traveller@test.com`
2. Click **Create Trip** in sidebar → `/traveller/trips/create`
3. Fill the form with test data above
4. Click Submit

**API called:** `POST /api/v1/traveller-trips`  
**Expected result:** Trip created with status `DRAFT`

**Test 2: View my trips**

1. Click **My Trips** in sidebar → `/traveller/trips`

**API called:** `GET /api/v1/traveller-trips/my`

**Test 3: Publish the trip**

1. On My Trips page, click **Publish**

**API called:** `POST /api/v1/traveller-trips/{id}/publish`  
**Expected result:** Status → `PUBLISHED`

**Test 4: View trip detail**

1. Click on the trip row → `/traveller/trips/:id`

**API called:** `GET /api/v1/traveller-trips/{id}`

**Test 5: Cancel a trip**

1. On trip detail, click **Cancel**

**API called:** `POST /api/v1/traveller-trips/{id}/cancel`  
**Expected result:** Status → `CANCELLED`

### Trip status transitions

```
DRAFT → PUBLISHED (via publish)
PUBLISHED → MATCHED (after booking is accepted)
PUBLISHED → CANCELLED (via cancel)
MATCHED → CLOSED (after booking completes)
```

### Negative tests for Traveller Trips

| Test | Expected |
|------|----------|
| Traveller without approved KYC tries to create trip | 400 — "Traveller must have approved KYC" |
| Buyer navigates to `/traveller/trips/create` | Redirected to buyer dashboard |
| Travel date in the past | 400 validation error |
| Missing source/destination | 400 validation error |
| Cancel a MATCHED or CLOSED trip | 400 — invalid status transition |

---

## 11. Matching Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Buyer Request Detail | `/buyer/requests/:id` | BUYER |
| Buyer Matches Page | `/buyer/matches` | BUYER |
| Traveller Trip Detail | `/traveller/trips/:id` | TRAVELLER |
| Traveller Matches Page | `/traveller/matches` | TRAVELLER |

### How matching works

The matching engine compares:
- Source country and city match
- Destination country and city match
- Travel date vs buyer's deadline date
- Item category compatibility

A **match score** (0–100) is calculated. Higher scores appear first.

### Step-by-step: Buyer generates matches

**Pre-condition:** Buyer must have a PUBLISHED request AND Traveller must have a PUBLISHED trip on the same route.

1. Login as `buyer@test.com`
2. Click **My Requests** → open the published request → `/buyer/requests/:id`
3. Click **Generate Matches**

**API called:** `POST /api/v1/matches/generate/by-request/{buyerRequestId}`  
**Expected result:** List of matching traveller trips with scores shown

4. Navigate to `/buyer/matches` to see all matches

**API called:** `GET /api/v1/matches/by-request/{buyerRequestId}`

### Step-by-step: Traveller generates matches

1. Login as `traveller@test.com`
2. Click **My Trips** → open the published trip → `/traveller/trips/:id`
3. Click **Generate Matches**

**API called:** `POST /api/v1/matches/generate/by-trip/{travellerTripId}`  
**Expected result:** List of matching buyer requests with scores

4. Navigate to `/traveller/matches` to see all matches

**API called:** `GET /api/v1/matches/by-trip/{travellerTripId}`

### Match status transitions

```
SUGGESTED → VIEWED (when user opens the match detail)
VIEWED → OFFER_SENT (when traveller sends an offer)
SUGGESTED/VIEWED → REJECTED (if manually rejected)
```

### Negative tests for Matching

| Test | Expected |
|------|----------|
| Generate matches for a DRAFT request | 400 — request is not published |
| Generate matches for a CANCELLED trip | 400 — invalid status |
| No compatible route in system | Returns empty list (not an error) |
| Non-owner tries to generate matches for a request | 403 Forbidden |

---

## 12. Offer Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Traveller Trip Detail | `/traveller/trips/:id` | TRAVELLER |
| Traveller Matches | `/traveller/matches` | TRAVELLER |
| Buyer Offers | `/buyer/offers` | BUYER |
| Buyer Request Detail | `/buyer/requests/:id` | BUYER |

### Test data for offer

```
Traveller Fee: 2500
Item Price: 5000
Platform Fee: 500
Currency: PKR
Message: I can bring this item on my trip from Toronto.
```

**Expected total amount:** 5000 + 2500 + 500 = **8000 PKR**

> **Note:** In the backend `CreateOfferRequest`, only `buyerRequestId`, `travellerTripId`, `travellerFee`, and `message` are sent. The `itemPrice` and `platformFee` come from the buyer request and platform config, not user input.

### Step-by-step: Traveller sends an offer

1. Login as `traveller@test.com`
2. Go to `/traveller/matches` or open Trip Detail → `/traveller/trips/:id`
3. Find the matching buyer request
4. Click **Send Offer**
5. Fill the offer form with travellerFee: 2500, message as above
6. Submit

**API called:** `POST /api/v1/offers`  
**Request body:**
```json
{
  "buyerRequestId": "<uuid>",
  "travellerTripId": "<uuid>",
  "travellerFee": 2500,
  "message": "I can bring this item on my trip from Toronto."
}
```
**Expected result:**
- Offer status → `SENT`
- Buyer receives `OFFER_SENT` notification
- Match status → `OFFER_SENT`

### Step-by-step: Buyer views and accepts offer

1. Login as `buyer@test.com`
2. Click **Offers** in sidebar → `/buyer/offers`
3. See the offer from the traveller

**API called:** `GET /api/v1/offers/by-request/{buyerRequestId}`

4. Click **Accept**

**API called:** `POST /api/v1/offers/{id}/accept`  
**Expected result:**
- Offer status → `ACCEPTED`
- Booking automatically created with status `PAYMENT_PENDING`
- Buyer Request status → `BOOKED`
- Traveller Trip status → `MATCHED`
- Traveller receives `OFFER_ACCEPTED` notification

### Step-by-step: Buyer rejects an offer

1. On the Offers page, click **Reject** on an offer

**API called:** `POST /api/v1/offers/{id}/reject`  
**Expected result:** Offer status → `REJECTED`

### Step-by-step: Traveller cancels their own offer

1. Login as traveller, go to My Offers
2. Click **Cancel** on a SENT offer

**API called:** `POST /api/v1/offers/{id}/cancel`  
**Expected result:** Offer status → `CANCELLED`

### Offer status transitions

```
SENT → ACCEPTED (buyer accepts)
SENT → REJECTED (buyer rejects)
SENT → CANCELLED (traveller cancels)
SENT → EXPIRED (system expires after timeout)
```

### Negative tests for Offers

| Test | Expected |
|------|----------|
| Buyer tries to send offer | 403 — only TRAVELLER role allowed |
| Traveller sends offer for a DRAFT request | 400 — invalid request status |
| Traveller sends duplicate offer for same request | 409 Conflict |
| Accept an already ACCEPTED offer | 400 — invalid status |
| Accept offer on wrong buyer's request | 403 Forbidden |

---

## 13. Booking Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Buyer Bookings | `/buyer/bookings` | BUYER |
| Traveller Bookings | `/traveller/bookings` | TRAVELLER |
| Booking Detail | `/shared/bookings/:id` | Both |

### When is a booking created?

A booking is created automatically when a Buyer accepts an offer (`POST /api/v1/offers/{id}/accept`). There is no separate "create booking" screen.

### Step-by-step: View bookings

**Buyer:**
1. Login as `buyer@test.com`
2. Click **Bookings** in sidebar → `/buyer/bookings`

**API called:** `GET /api/v1/bookings/my`

3. Click on a booking row to view details → `/shared/bookings/:id`

**API called:** `GET /api/v1/bookings/{id}`

**Traveller:**
1. Login as `traveller@test.com`
2. Click **Bookings** in sidebar → `/traveller/bookings`
3. View the same booking from their perspective

### Booking fields

| Field | Description |
|-------|-------------|
| `buyerRequestTitle` | Title of the item request |
| `buyerName` / `travellerName` | Parties involved |
| `itemPrice` | Item cost |
| `travellerFee` | Traveller's fee |
| `platformFee` | Platform commission |
| `totalAmount` | Sum of all three |
| `currency` | Currency code (e.g., PKR) |
| `status` | Current booking status |

### Booking status transitions

```
PAYMENT_PENDING → PAYMENT_HELD (after buyer pays)
PAYMENT_HELD → IN_TRANSIT (traveller marks in transit)
IN_TRANSIT → DELIVERED_PENDING_VERIFICATION (traveller marks delivered)
DELIVERED_PENDING_VERIFICATION → COMPLETED (delivery code verified)
Any active status → DISPUTED (dispute raised)
PAYMENT_PENDING → CANCELLED (before payment)
```

### Negative tests for Bookings

| Test | Expected |
|------|----------|
| Cancel a booking after payment is held | 400 — cannot cancel paid booking |
| Accept an offer twice | 400 — booking already exists |
| Non-party user views booking detail | 403 Forbidden |

---

## 14. Payment & Escrow Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Booking Detail | `/shared/bookings/:id` | BUYER |
| Payment Page | `/shared/payments/:bookingId` | BUYER |
| Admin Payments | `/admin/payments` | ADMIN |

### How payments work

1. Booking is created with status `PAYMENT_PENDING`
2. Buyer navigates to the payment page
3. Buyer pays (simulated in development — no real card required)
4. Payment is **HELD in escrow** — the money is held by the platform, not yet given to traveller
5. After successful delivery verification, Admin or system **RELEASES** the payment to the traveller

### Step-by-step: Buyer pays

1. Login as `buyer@test.com`
2. Open Booking Detail → `/shared/bookings/:id`
3. The booking should show status `PAYMENT_PENDING`
4. Click **Pay** (or navigate to `/shared/payments/:bookingId`)
5. Confirm the payment amount (this is simulated in dev — no real card needed)
6. Click **Confirm Payment**

**API called:** `POST /api/v1/payments/{bookingId}/pay`  
**Expected result:**
- Payment record created with status `HELD`
- Booking status → `PAYMENT_HELD`
- Buyer and Traveller both receive `PAYMENT_HELD` notification

**Check payment status:**

**API called:** `GET /api/v1/payments/{bookingId}`  
**Expected response includes:** `"status": "HELD"`

### Payment fields

| Field | Value |
|-------|-------|
| `amount` | totalAmount from booking |
| `currency` | PKR (or as set in offer) |
| `status` | HELD (after payment) |
| `buyerId` | Buyer's user ID |
| `travellerId` | Traveller's user ID |

### Step-by-step: Admin releases payment (after delivery verified)

In this system, payment is released automatically after successful delivery code verification. Admin can also release manually:

1. Login as `admin@test.com`
2. Click **Payments** in Admin sidebar → `/admin/payments`
3. Find the payment with status `HELD`
4. Click **Release**

**API called:** `POST /api/v1/payments/{paymentId}/release`  
**Expected result:** Payment status → `RELEASED`

### Step-by-step: Admin refunds payment

1. On Admin Payments page, find a HELD payment
2. Click **Refund**

**API called:** `POST /api/v1/admin/payments/{paymentId}/refund`  
**Expected result:** Payment status → `REFUNDED`

### Payment status transitions

```
PENDING → HELD (buyer pays)
HELD → RELEASED (delivery verified or admin releases)
HELD → REFUNDED (admin refunds)
PENDING → FAILED (payment provider failure)
```

### Negative tests for Payments

| Test | Expected |
|------|----------|
| Traveller tries to pay for booking | 403 — only BUYER role allowed |
| Buyer pays for a booking they don't own | 403 Forbidden |
| Pay same booking twice | 409 Conflict — payment already exists |
| Release a RELEASED payment | 400 — invalid status |
| Refund a RELEASED payment | 400 — cannot refund completed payment |

---

## 15. Delivery Verification Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Delivery Verification | `/shared/delivery/:id` | BUYER & TRAVELLER |
| Traveller Bookings | `/traveller/bookings` | TRAVELLER |

### How delivery verification works

1. **Buyer generates** a 6-digit code (after payment is held)
2. **Buyer shows/tells** the code to the Traveller at the time of physical delivery
3. **Traveller enters** the code on their screen to confirm delivery
4. **System verifies** the code, marks booking as COMPLETED, and **releases payment**

### Step-by-step: Buyer generates delivery code

**Pre-condition:** Booking status must be `PAYMENT_HELD`

1. Login as `buyer@test.com`
2. Navigate to `/shared/delivery/:bookingId`
3. Click **Generate Code**

**API called:** `POST /api/v1/delivery-codes/{bookingId}/generate`  
**Expected response:**
```json
{
  "data": {
    "code": "847231",
    "bookingId": "...",
    "expiresAt": "2026-08-15T15:00:00Z"
  }
}
```
**Expected result:** 6-digit code displayed to buyer. Code status → `ACTIVE`

### Step-by-step: Traveller marks booking steps

**Step 1: Mark In Transit**

1. Login as `traveller@test.com`
2. Click **Bookings** → `/traveller/bookings`
3. Find the booking with status `PAYMENT_HELD`
4. Click **Mark In Transit**

**API called:** `POST /api/v1/bookings/{id}/mark-in-transit`  
**Expected result:** Booking status → `IN_TRANSIT`

**Step 2: Mark Delivered**

1. When physically at delivery location, click **Mark Delivered**

**API called:** `POST /api/v1/bookings/{id}/mark-delivered`  
**Expected result:** Booking status → `DELIVERED_PENDING_VERIFICATION`

**Step 3: Verify Code**

1. Navigate to `/shared/delivery/:bookingId`
2. Enter the 6-digit code given by the buyer
3. Click **Verify Code**

**API called:** `POST /api/v1/delivery-codes/{bookingId}/verify`  
**Request body:** `{ "code": "847231" }`  
**Expected result:**
- Delivery code status → `USED`
- Booking status → `COMPLETED`
- Payment status → `RELEASED`
- Both buyer and traveller receive `DELIVERY_VERIFIED` and `PAYMENT_RELEASED` notifications

### Check delivery code status

**API called:** `GET /api/v1/delivery-codes/{bookingId}/status`  
**Response fields:** `codeGenerated`, `verified`, `attemptsUsed`

### Delivery code status transitions

```
ACTIVE → USED (code successfully verified)
ACTIVE → EXPIRED (code expired before use — configurable time limit)
```

### Negative tests for Delivery

| Test | Expected |
|------|----------|
| Generate code before payment is held | 400 — booking not in correct status |
| Enter wrong code | Error — "Invalid code", attempts counter increments |
| Enter same correct code twice | 400 — code already USED |
| Enter correct code from expired code | 400 — code is EXPIRED |
| Buyer tries to verify code | 403 — only TRAVELLER role can verify |
| Traveller generates code | 403 — only BUYER role can generate |
| Too many wrong attempts (>5 in 10 min) | 429 — Too Many Requests (rate limited) |

---

## 16. Notification Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Notifications Page | `/buyer/notifications` | BUYER |
| Notifications Page | `/traveller/notifications` | TRAVELLER |
| Admin Notifications | `/admin/notifications` | ADMIN |

### When are notifications created?

| Event | Who receives notification | Type |
|-------|--------------------------|------|
| KYC submitted | Admin | `KYC_SUBMITTED` |
| KYC approved | Traveller | `KYC_APPROVED` |
| KYC rejected | Traveller | `KYC_REJECTED` |
| Match found | Both | `MATCH_FOUND` |
| Offer sent | Buyer | `OFFER_SENT` |
| Offer accepted | Traveller | `OFFER_ACCEPTED` |
| Offer rejected | Traveller | `OFFER_REJECTED` |
| Booking created | Both | `BOOKING_CREATED` |
| Payment held | Both | `PAYMENT_HELD` |
| Delivery code generated | Traveller | `DELIVERY_CODE_GENERATED` |
| Delivery verified | Buyer | `DELIVERY_VERIFIED` |
| Payment released | Traveller | `PAYMENT_RELEASED` |
| Dispute created | Admin + other party | `DISPUTE_CREATED` |

### Step-by-step testing

**Test 1: View notifications**

1. Login as `buyer@test.com`
2. Click **Notifications** in sidebar → `/buyer/notifications`

**API called:** `GET /api/v1/notifications/my`

Expected: List of notifications with title, message, and read/unread status

**Test 2: Check unread count**

The Topbar bell icon shows the unread count.

**API called:** `GET /api/v1/notifications/unread-count`  
**Expected response:** `{ "data": { "unreadCount": 3 } }`

**Test 3: Mark one notification as read**

Click on a notification or click its "Mark Read" button.

**API called:** `POST /api/v1/notifications/{id}/read`  
**Expected result:** Notification status → `READ`, unread count decreases

**Test 4: Mark all as read**

Click **Mark All as Read** button.

**API called:** `POST /api/v1/notifications/read-all`  
**Expected result:** All notifications marked READ, unread count → 0

### Admin: Send manual notification

1. Login as admin
2. Go to `/admin/notifications`
3. Click **Send Notification**
4. Fill user ID, title, message, type
5. Submit

**API called:** `POST /api/v1/admin/notifications/send`

### Negative tests for Notifications

| Test | Expected |
|------|----------|
| Read another user's notification | 403 Forbidden |
| Unauthenticated user hits `/api/v1/notifications/my` | 401 Unauthorized |

---

## 17. Chat Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Chat Page | `/shared/chat` | BUYER & TRAVELLER |

### How chat works

- Each booking gets one chat room
- Buyer and Traveller can message each other about the booking
- Chat room is created by calling `POST /api/v1/chat/rooms/{bookingId}` — if it already exists, it returns the existing room

### Step-by-step testing

**Test 1: Open/create chat room**

1. Login as `buyer@test.com`
2. Click **Chat** in sidebar → `/shared/chat`

**API called:** `GET /api/v1/chat/rooms/my` (loads list of chat rooms)

3. Select a room or create one for a booking

**API called:** `POST /api/v1/chat/rooms/{bookingId}` (creates or returns existing room)

**Test 2: Send a message (Buyer)**

1. In the chat room, type: "Hello, when will you deliver my item?"
2. Press Enter or click Send

**API called:** `POST /api/v1/chat/rooms/{roomId}/messages`  
**Request body:** `{ "content": "Hello, when will you deliver my item?" }`

**Test 3: Read messages (Traveller)**

1. Login as `traveller@test.com`
2. Click **Chat** → `/shared/chat`
3. Open the same booking's chat room

**API called:** `GET /api/v1/chat/rooms/{roomId}/messages`

4. See the buyer's message
5. Reply: "I will deliver it on Monday!"

**Test 4: Check unread count**

**API called:** `GET /api/v1/chat/rooms/{roomId}/unread-count`

**Test 5: Mark messages as read**

**API called:** `POST /api/v1/chat/rooms/{roomId}/read`  
**Expected result:** `{ "markedAsRead": 1 }` (number of messages marked)

### Negative tests for Chat

| Test | Expected |
|------|----------|
| User not in booking opens chat room | 403 Forbidden |
| Send blank message | 400 validation error |
| Admin closes a chat room | `POST /api/v1/chat/rooms/{roomId}/close` → Room status → CLOSED |

---

## 18. Review Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Reviews Page | `/shared/reviews` | BUYER & TRAVELLER |

### When can a review be written?

Reviews can only be written after a booking is **COMPLETED**. One review per booking per user.

### Test data

```
Booking: (completed booking ID)
Rating: 5
Comment: Excellent service, very professional traveller!
```

### Step-by-step testing

**Test 1: Buyer reviews traveller**

**Pre-condition:** Booking must be in `COMPLETED` status.

1. Login as `buyer@test.com`
2. Click **Reviews** in sidebar → `/shared/reviews`
3. Find the completed booking
4. Select rating (1–5 stars), add comment
5. Click **Submit Review**

**API called:** `POST /api/v1/reviews`  
**Request body:**
```json
{
  "bookingId": "<uuid>",
  "rating": 5,
  "comment": "Excellent service!"
}
```
**Expected result:** Review saved, traveller's average rating updated

**Test 2: Traveller reviews buyer**

1. Login as `traveller@test.com`
2. Same process on Reviews page

**Test 3: View reviews for a user**

**API called:** `GET /api/v1/reviews/user/{userId}`

**Test 4: View rating summary**

**API called:** `GET /api/v1/reviews/user/{userId}/summary`  
**Expected response:**
```json
{
  "data": {
    "userId": "...",
    "totalReviews": 1,
    "averageRating": 5.0,
    "ratingBreakdown": { "5": 1 }
  }
}
```

**Test 5: View my received reviews**

**API called:** `GET /api/v1/reviews/my-received`

**Test 6: View reviews I have given**

**API called:** `GET /api/v1/reviews/my-given`

### Negative tests for Reviews

| Test | Expected |
|------|----------|
| Review before booking is COMPLETED | 400 — booking not completed |
| Review same booking twice | 409 — review already exists |
| Rating of 0 or 6 | 400 validation error |
| Review for a booking you are not part of | 403 Forbidden |

---

## 19. Dispute Testing

### Frontend screens involved

| Screen | URL | Role |
|--------|-----|------|
| Disputes Page | `/shared/disputes` | BUYER & TRAVELLER |
| Admin Disputes | `/admin/disputes` | ADMIN |

### Dispute reasons

The system supports these dispute reasons:
- `ITEM_NOT_DELIVERED`
- `WRONG_ITEM`
- `DAMAGED_ITEM`
- `LATE_DELIVERY`
- `PAYMENT_ISSUE`
- `FRAUD_SUSPICION`
- `BEHAVIOUR_ISSUE`
- `OTHER`

### Step-by-step: Create a dispute

**Pre-condition:** User must have an active booking.

1. Login as `buyer@test.com`
2. Click **Disputes** in sidebar → `/shared/disputes`
3. Click **Raise Dispute** or **New Dispute**
4. Fill the form:
   - Booking ID: (select from dropdown or enter ID)
   - Reason: ITEM_NOT_DELIVERED
   - Description: "The traveller has not delivered the item after 2 weeks."
5. Click **Submit**

**API called:** `POST /api/v1/disputes`  
**Request body:**
```json
{
  "bookingId": "<uuid>",
  "reason": "ITEM_NOT_DELIVERED",
  "description": "The traveller has not delivered the item after 2 weeks."
}
```
**Expected result:**
- Dispute status → `OPEN`
- Booking status → `DISPUTED`
- Notifications sent to both parties and admin

### Step-by-step: Admin manages dispute

1. Login as `admin@test.com`
2. Click **Disputes** in Admin sidebar → `/admin/disputes`
3. See the OPEN dispute

**Step A: Mark Under Review**

Click **Mark Under Review** on the dispute.

**API called:** `POST /api/v1/admin/disputes/{id}/mark-under-review`  
**Expected result:** Dispute status → `UNDER_REVIEW`

**Step B: Resolve Dispute**

Click **Resolve**, enter resolution note.

**API called:** `POST /api/v1/admin/disputes/{id}/resolve`  
**Request body:** `{ "resolution": "Buyer refunded. Traveller penalised.", "refundBuyer": true }`  
**Expected result:** Dispute status → `RESOLVED`

**Step C: Reject Dispute**

If dispute is invalid, click **Reject**, enter reason.

**API called:** `POST /api/v1/admin/disputes/{id}/reject`  
**Request body:** `{ "reason": "Insufficient evidence provided." }`  
**Expected result:** Dispute status → `REJECTED`

### Dispute status transitions

```
OPEN → UNDER_REVIEW (admin marks)
UNDER_REVIEW → RESOLVED (admin resolves)
OPEN / UNDER_REVIEW → REJECTED (admin rejects)
```

### Negative tests for Disputes

| Test | Expected |
|------|----------|
| Create duplicate dispute for same active booking | 409 Conflict |
| User not in booking raises dispute | 403 Forbidden |
| Resolve an already RESOLVED dispute | 400 — invalid status |
| Submit dispute with no description | 400 validation error |
| Non-admin calls `POST /api/v1/admin/disputes/{id}/resolve` | 403 Forbidden |

---

## 20. Admin Panel Testing

### Frontend screens involved (Admin only)

| Screen | URL |
|--------|-----|
| Admin Dashboard | `/admin/dashboard` |
| Admin Users | `/admin/users` |
| Admin KYC | `/admin/kyc` |
| Admin Buyer Requests | `/admin/buyer-requests` |
| Admin Traveller Trips | `/admin/traveller-trips` |
| Admin Bookings | `/admin/bookings` |
| Admin Payments | `/admin/payments` |
| Admin Disputes | `/admin/disputes` |
| Admin Notifications | `/admin/notifications` |
| Admin Audit Logs | `/admin/audit-logs` |

### Test 1: Dashboard Summary

1. Login as `admin@test.com`
2. Open `/admin/dashboard`

**API called:** `GET /api/v1/admin/dashboard/summary`  
**Expected result:** Count cards showing total users, KYC pending, total buyer requests, trips, bookings, payments, disputes, reviews, etc.

**API called:** `GET /api/v1/admin/dashboard/recent-activity`  
**Expected result:** Recent audit log entries visible

### Test 2: User Management

1. Go to `/admin/users`

**API called:** `GET /api/v1/admin/users`  
**Expected result:** Table showing all users with role, status, email

**Disable a user:**  
Click **Disable** on a user (not admin@test.com!)  
**API called:** `POST /api/v1/admin/users/{userId}/disable`  
**Request body:** `{ "reason": "Suspicious activity" }`  
**Expected result:** User `accountStatus` → `DISABLED`

**Enable a user:**  
Click **Enable**  
**API called:** `POST /api/v1/admin/users/{userId}/enable`  
**Expected result:** User `accountStatus` → `ACTIVE`

**Change user role:**  
Click **Change Role**, select new role  
**API called:** `POST /api/v1/admin/users/{userId}/change-role`  
**Request body:** `{ "role": "ADMIN" }`

### Test 3: Admin Bookings

1. Go to `/admin/bookings`

**API called:** `GET /api/v1/admin/bookings`  
**Expected result:** All bookings in the system with status filter

**Cancel a booking (Admin):**  
**API called:** `POST /api/v1/admin/bookings/{bookingId}/cancel`  
**Request body:** `{ "reason": "Policy violation" }`

### Test 4: Audit Logs

1. Go to `/admin/audit-logs`

**API called:** `GET /api/v1/admin/audit-logs`  
**Expected result:** Timestamped log of all admin actions (KYC approvals, user disables, payment releases, etc.)

### Negative tests for Admin

| Test | Expected |
|------|----------|
| Buyer opens `/admin/dashboard` in browser | Redirected to `/buyer/dashboard` (role guard) |
| Buyer hits `GET /api/v1/admin/users` directly | 403 Forbidden |
| Admin disables their own account | Should be prevented (or handle carefully) |
| Unauthenticated request to any admin endpoint | 401 Unauthorized |

---

## 21. Complete Status Flow Reference

### Buyer Request Status

| Status | Meaning | Next statuses |
|--------|---------|---------------|
| `DRAFT` | Created, not public | `PUBLISHED`, `CANCELLED` (delete also allowed) |
| `PUBLISHED` | Visible to travellers for matching | `BOOKED`, `CANCELLED` |
| `MATCHED` | Matched with traveller (intermediate) | `BOOKED` |
| `BOOKED` | Offer accepted, booking created | `COMPLETED` |
| `COMPLETED` | Full journey done | — (terminal) |
| `CANCELLED` | Cancelled | — (terminal) |

### Traveller Trip Status

| Status | Meaning | Next statuses |
|--------|---------|---------------|
| `DRAFT` | Created, not public | `PUBLISHED`, `CANCELLED` (delete allowed) |
| `PUBLISHED` | Visible for matching | `MATCHED`, `CANCELLED` |
| `MATCHED` | Booking accepted | `CLOSED` |
| `CLOSED` | Trip completed | — (terminal) |
| `CANCELLED` | Cancelled | — (terminal) |

### Match Status

| Status | Meaning |
|--------|---------|
| `SUGGESTED` | Newly generated match |
| `VIEWED` | User has seen the match |
| `OFFER_SENT` | Traveller sent offer based on this match |
| `REJECTED` | Match manually rejected |

### Offer Status

| Status | Meaning |
|--------|---------|
| `SENT` | Traveller sent, awaiting buyer response |
| `ACCEPTED` | Buyer accepted → booking created |
| `REJECTED` | Buyer rejected |
| `CANCELLED` | Traveller cancelled |
| `EXPIRED` | System expired after timeout |

### Booking Status

| Status | Meaning |
|--------|---------|
| `PAYMENT_PENDING` | Offer accepted, waiting for buyer to pay |
| `PAYMENT_HELD` | Payment made, held in escrow |
| `IN_TRANSIT` | Traveller marked as in transit |
| `DELIVERED_PENDING_VERIFICATION` | Traveller marked delivered, waiting for code verification |
| `DELIVERED` | Intermediate delivery state |
| `COMPLETED` | Code verified, payment released |
| `CANCELLED` | Cancelled before payment |
| `DISPUTED` | Dispute raised by buyer or traveller |

### Payment Status

| Status | Meaning |
|--------|---------|
| `PENDING` | Payment initiated |
| `AUTHORIZED` | Payment authorized by provider |
| `HELD` | Money in escrow |
| `RELEASED` | Payment sent to traveller |
| `REFUNDED` | Money returned to buyer |
| `FAILED` | Payment failed |

### Delivery Code Status

| Status | Meaning |
|--------|---------|
| `ACTIVE` | Valid, can be used |
| `USED` | Successfully verified |
| `EXPIRED` | Time limit passed |

### KYC Status

| Status | Meaning |
|--------|---------|
| `NOT_SUBMITTED` | No submission yet |
| `PENDING_REVIEW` | Submitted, awaiting admin |
| `APPROVED` | Traveller can now create trips |
| `REJECTED` | Must resubmit |
| `EXPIRED` | KYC expired after long period |

### Dispute Status

| Status | Meaning |
|--------|---------|
| `OPEN` | Raised, not yet reviewed |
| `UNDER_REVIEW` | Admin is reviewing |
| `RESOLVED` | Admin resolved with outcome |
| `REJECTED` | Admin rejected (invalid dispute) |

### Notification Status

| Status | Meaning |
|--------|---------|
| `PENDING` | Queued to send |
| `SENT` | Delivered (same as UNREAD in frontend) |
| `READ` | User has read it |
| `FAILED` | Delivery failed |

---

## 22. API Testing via Browser DevTools

Use your browser's Developer Tools to inspect every API call the frontend makes.

### How to open DevTools

Press **F12** (or right-click → Inspect) → Click the **Network** tab.

### What to look at

| Column | What to check |
|--------|---------------|
| Name | The API path (e.g., `/api/v1/auth/login`) |
| Method | GET / POST / PUT / DELETE |
| Status | Should be 200 or 201 for success |
| Initiator | Which frontend file triggered the call |

### Checking a request in detail

1. Click on a request row
2. **Headers tab:** Check `Authorization: Bearer eyJ...` is present for protected endpoints
3. **Payload tab:** Check the request body JSON
4. **Response tab:** Check `success: true` and `data` fields
5. **Preview tab:** Formatted view of the response

### Common HTTP status codes

| Code | Meaning | What to do |
|------|---------|-----------|
| 200 | Success | All good |
| 201 | Created | Resource was created |
| 400 | Bad Request | Check request body, fix validation errors |
| 401 | Unauthorized | Not logged in, or token expired — re-login |
| 403 | Forbidden | Logged in but wrong role, or accessing someone else's data |
| 404 | Not Found | Wrong ID or endpoint doesn't exist |
| 409 | Conflict | Duplicate resource (e.g., same email registered twice) |
| 429 | Too Many Requests | Rate limit hit — wait before retrying |
| 500 | Server Error | Backend bug — check backend terminal logs |

### Checking for CORS errors

If you see a red entry with text `CORS` or `ERR_FAILED` before the request details:
1. Go to backend terminal
2. Look for `AccessDeniedException: Access Denied` on an OPTIONS request
3. Fix: Add `http://localhost:5174` to `app.cors.allowed-origins` in `application-local.yml`
4. Restart backend

---

## 23. Database Verification

After testing, verify the data in the database directly.

### Connect to the database

```bash
docker exec -it marketplace-postgres psql -U marketplace -d marketplace
```

### Actual table names (from Flyway migrations)

| Table | What it stores |
|-------|---------------|
| `users` | All user accounts |
| `refresh_tokens` | JWT refresh tokens |
| `buyer_requests` | Buyer item requests |
| `traveller_trips` | Traveller trip listings |
| `matches` | Request-trip matches |
| `offers` | Offers from travellers |
| `bookings` | Confirmed bookings |
| `payments` | Payment records |
| `delivery_verification_codes` | 6-digit delivery codes |
| `notifications` | User notifications |
| `reviews` | Buyer/traveller reviews |
| `disputes` | Raised disputes |
| `chat_rooms` | Chat rooms per booking |
| `chat_messages` | Messages in chat rooms |
| `audit_logs` | Admin action audit trail |
| `kyc_documents` | KYC submissions |

### Useful SELECT queries

```sql
-- View all users
SELECT id, email, role, account_status, created_at FROM users;

-- Count users by role
SELECT role, COUNT(*) FROM users GROUP BY role;

-- View buyer requests
SELECT id, title, status, buyer_id FROM buyer_requests ORDER BY created_at DESC;

-- View traveller trips
SELECT id, source_city, destination_city, travel_date, status FROM traveller_trips ORDER BY created_at DESC;

-- View matches with score
SELECT id, buyer_request_id, traveller_trip_id, match_score, status FROM matches ORDER BY match_score DESC;

-- View offers
SELECT id, buyer_request_id, traveller_fee, total_amount, status FROM offers ORDER BY created_at DESC;

-- View bookings with status
SELECT id, buyer_id, traveller_id, total_amount, status FROM bookings ORDER BY created_at DESC;

-- View bookings grouped by status
SELECT status, COUNT(*) FROM bookings GROUP BY status;

-- View payments grouped by status
SELECT status, COUNT(*) FROM payments GROUP BY status;

-- View delivery codes
SELECT booking_id, status, attempts_used, expires_at FROM delivery_verification_codes;

-- View KYC submissions
SELECT id, user_id, status, document_type FROM kyc_documents ORDER BY created_at DESC;

-- View notifications for a user (replace email with actual)
SELECT n.title, n.type, n.status FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE u.email = 'buyer@test.com'
ORDER BY n.created_at DESC;

-- View audit logs
SELECT actor_email, action, entity_type, created_at FROM audit_logs ORDER BY created_at DESC LIMIT 20;

-- View disputes
SELECT id, booking_id, reason, status FROM disputes ORDER BY created_at DESC;

-- View reviews
SELECT reviewer_id, reviewee_id, rating, comment FROM reviews ORDER BY created_at DESC;

-- View chat messages
SELECT cm.content, cm.sent_at FROM chat_messages cm
JOIN chat_rooms cr ON cm.room_id = cr.id
ORDER BY cm.sent_at DESC LIMIT 10;
```

### Exit psql

```sql
\q
```

---

## 24. End-to-End Testing Checklist

Use this checklist when running a full end-to-end test.

### Auth
- [ ] Buyer registered successfully
- [ ] Traveller registered successfully
- [ ] Admin login works (after DB role update)
- [ ] BUYER → redirected to `/buyer/dashboard`
- [ ] TRAVELLER → redirected to `/traveller/dashboard`
- [ ] ADMIN → redirected to `/admin/dashboard`
- [ ] Logout clears tokens and redirects to `/login`

### Profile
- [ ] Buyer profile page loads (`/buyer/profile`)
- [ ] Traveller profile page loads (`/traveller/profile`)

### KYC
- [ ] Traveller navigates to `/traveller/kyc`
- [ ] KYC submitted (or DB insert used as workaround)
- [ ] Admin sees pending KYC at `/admin/kyc`
- [ ] Admin approves KYC
- [ ] Traveller KYC status shows APPROVED
- [ ] Traveller receives KYC_APPROVED notification

### Buyer Request
- [ ] Buyer creates request → status DRAFT
- [ ] Buyer publishes request → status PUBLISHED
- [ ] Request appears in My Requests (`/buyer/requests`)
- [ ] Request detail page loads (`/buyer/requests/:id`)

### Traveller Trip
- [ ] Traveller creates trip → status DRAFT
- [ ] Traveller publishes trip → status PUBLISHED
- [ ] Trip appears in My Trips (`/traveller/trips`)
- [ ] Trip detail page loads (`/traveller/trips/:id`)

### Matching
- [ ] Buyer generates matches for published request
- [ ] Traveller generates matches for published trip
- [ ] Match score visible
- [ ] Same match appears from both buyer and traveller perspectives

### Offer
- [ ] Traveller sends offer from trip detail or matches page
- [ ] Offer status SENT
- [ ] Buyer sees offer at `/buyer/offers`
- [ ] Buyer accepts offer
- [ ] Offer status → ACCEPTED

### Booking
- [ ] Booking automatically created after offer accepted
- [ ] Booking visible at `/buyer/bookings` and `/traveller/bookings`
- [ ] Booking status → PAYMENT_PENDING
- [ ] Booking detail shows correct amounts

### Payment
- [ ] Buyer navigates to `/shared/payments/:bookingId`
- [ ] Buyer pays — no errors
- [ ] Payment status → HELD
- [ ] Booking status → PAYMENT_HELD

### Delivery
- [ ] Buyer generates delivery code at `/shared/delivery/:id`
- [ ] 6-digit code displayed
- [ ] Traveller marks booking IN_TRANSIT
- [ ] Traveller marks booking DELIVERED_PENDING_VERIFICATION
- [ ] Traveller enters correct code at `/shared/delivery/:id`
- [ ] Code verified successfully
- [ ] Booking status → COMPLETED
- [ ] Payment status → RELEASED

### Reviews
- [ ] Buyer submits review for traveller at `/shared/reviews`
- [ ] Traveller submits review for buyer
- [ ] Rating summary updated

### Disputes
- [ ] Dispute created at `/shared/disputes`
- [ ] Admin sees dispute at `/admin/disputes`
- [ ] Admin marks UNDER_REVIEW
- [ ] Admin resolves dispute

### Chat
- [ ] Buyer opens chat at `/shared/chat`
- [ ] Buyer sends message
- [ ] Traveller opens same chat room
- [ ] Traveller sees buyer message
- [ ] Traveller replies
- [ ] Unread count shows on bell icon

### Notifications
- [ ] Notifications appear after each major action
- [ ] Unread count badge shows on bell icon
- [ ] Mark as read works
- [ ] Mark all as read works

### Admin Panel
- [ ] Dashboard summary counts visible
- [ ] User list loads
- [ ] Disable/enable user works
- [ ] Audit logs visible with correct actions

---

## 25. Common Errors and Fixes

| Problem | Possible Cause | Fix |
|---------|---------------|-----|
| Frontend cannot connect to backend | Backend not running | Run: `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` from `backend/` folder |
| CORS error in browser console | Frontend port not in allowed origins | Add `http://localhost:5174` to `app.cors.allowed-origins` in `application-local.yml`, restart backend |
| 401 Unauthorized on every request | JWT token not sent or expired | Check `tm_access_token` exists in `localStorage` (F12 → Application → Local Storage). Re-login if missing |
| 401 after 15 minutes | Access token expired, refresh failed | Re-login; if automatic refresh is broken, check `/api/v1/auth/refresh` response |
| 403 Forbidden | Wrong role accessing an endpoint | Check you are logged in as the correct role (Buyer vs Traveller vs Admin) |
| 403 on KYC endpoint from Traveller | No user-facing KYC controller exists | Use DB workaround to insert KYC record directly |
| 404 on `/kyc/submit` | Backend KYC user endpoint not implemented | Insert KYC manually into `kyc_documents` table via psql |
| 404 on `/profile/me` | Backend profile endpoint not implemented | Profile feature not yet backend-wired |
| Buyer cannot create request | Not logged in as BUYER role | Logout, login as buyer@test.com |
| Traveller cannot create/publish trip | KYC not approved | Approve KYC from Admin panel first |
| Match not generated | Request or trip not PUBLISHED, or wrong route | Publish both request and trip; ensure cities/countries match |
| Offer cannot be accepted | Offer status is not SENT, or already accepted | Check offer status in DB. Ensure only one offer is being accepted |
| Booking not ready for payment | Booking status is not PAYMENT_PENDING | Check booking status at `/shared/bookings/:id` |
| Payment already exists | Pay button clicked twice | Check DB: `SELECT * FROM payments WHERE booking_id = '...'` |
| Delivery code invalid | Wrong code entered | Ask buyer to share the 6-digit code from the Delivery Verification page |
| Delivery code expired | Code was not used within time limit | Generate a new code: `POST /api/v1/delivery-codes/{bookingId}/generate` |
| Payment not released after code verified | Code verification failed silently | Check backend logs for errors; check delivery code status endpoint |
| Notification not showing | Notification service down, or Kafka not running | Check `KAFKA_ENABLED` setting; check Docker containers |
| Chat room not found | Booking ID not used to create room | Call `POST /api/v1/chat/rooms/{bookingId}` first |
| Admin page shows "Forbidden" | User is not ADMIN role | Check DB: `SELECT role FROM users WHERE email = 'admin@test.com'` |
| Database table missing | Flyway migration not run | Check backend logs for Flyway errors at startup |
| Flyway migration failed | Migration SQL error or checksums mismatch | Check `backend/src/main/resources/db/migration/` files; never edit applied migrations |
| Docker container stopped | System restart or crash | Run: `docker compose up -d postgres redis` from `backend/` folder |
| Kafka not running (if enabled) | Container stopped | Run: `docker compose up -d zookeeper kafka`; or set `app.kafka.enabled: false` in `application-local.yml` |
| Port 8080 already in use | Previous backend process still running | Run: `Get-NetTCPConnection -LocalPort 8080 | Stop-Process` in PowerShell |
| `node` not found in terminal | PATH not refreshed after Node.js install | Close and reopen the terminal; or run a new PowerShell window |
| `npm run dev` fails | Wrong directory | Run from `traveller-marketplace-frontend/` folder |

---

## 26. Test Report Template

Use this template for each feature test you run.

### Single Test Report

```
Feature:          [e.g., Buyer Request - Create and Publish]
Tester:           [Your name]
Date:             [YYYY-MM-DD]
Environment:      Local
Frontend URL:     http://localhost:5174
Backend URL:      http://localhost:8080
Test Result:      PASS / FAIL

Steps Performed:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected Result:
[What should happen]

Actual Result:
[What actually happened]

Bug Screenshot:
[Attach screenshot if FAIL]

Notes:
[Any additional observations]
```

### Test Cases Table

Use this format to track multiple test cases:

| Test Case ID | Feature | Steps Summary | Expected Result | Actual Result | Status | Notes |
|---|---|---|---|---|---|---|
| TC-001 | Auth — Register Buyer | Fill form, submit | Redirected to buyer dashboard | Redirected to buyer dashboard | PASS | — |
| TC-002 | Auth — Register Traveller | Fill form, submit | Redirected to traveller dashboard | Redirected to traveller dashboard | PASS | — |
| TC-003 | Auth — Login wrong password | Enter wrong password | 401 error shown | 401 error shown | PASS | — |
| TC-004 | KYC — Submit | Fill KYC form, submit | Status PENDING_REVIEW | 404 error | FAIL | Backend endpoint not implemented |
| TC-005 | KYC — Admin Approve | Admin approves KYC | Status APPROVED | Status APPROVED | PASS | Used DB workaround |
| TC-006 | Buyer Request — Create | Fill form, save | Status DRAFT | Status DRAFT | PASS | — |
| TC-007 | Buyer Request — Publish | Click Publish | Status PUBLISHED | Status PUBLISHED | PASS | — |
| TC-008 | Traveller Trip — Create | Fill form, save | Status DRAFT | Status DRAFT | PASS | — |
| TC-009 | Matching — Generate | Click Generate Matches | Matches listed | Matches listed | PASS | — |
| TC-010 | Offer — Send | Traveller sends offer | Status SENT | Status SENT | PASS | — |
| TC-011 | Offer — Accept | Buyer accepts offer | Booking created | Booking created | PASS | — |
| TC-012 | Payment — Pay | Buyer pays | Payment HELD | Payment HELD | PASS | — |
| TC-013 | Delivery — Generate Code | Buyer generates code | 6-digit code shown | 6-digit code shown | PASS | — |
| TC-014 | Delivery — Verify Code | Traveller enters code | Booking COMPLETED | Booking COMPLETED | PASS | — |
| TC-015 | Review — Submit | Buyer rates traveller | Review saved | Review saved | PASS | — |

---

## 27. Final Notes

### Testing approach

1. **Always test the happy path first.** Get one full booking cycle from registration to payment release working before testing edge cases.

2. **Then test negative cases.** Try wrong inputs, wrong roles, duplicate actions.

3. **Always check three places:** Frontend screen, Browser Network tab, and Database.

4. **If one module fails, stop and fix it** before moving to the next module. Each module depends on the previous one.

5. **Keep screenshots** of any errors — include the URL, the browser console, and the Network tab response.

6. **Check backend logs** whenever you get a 500 error. The backend terminal shows the full stack trace.

### Known frontend-backend gaps (as of this guide version)

| Frontend calls | Backend endpoint | Status |
|---|---|---|
| `POST /api/v1/kyc/submit` | Does not exist (no user-facing KYC controller) | Use DB workaround |
| `GET /api/v1/kyc/my` | Does not exist | Use admin endpoint |
| `GET /api/v1/profile/me` | Does not exist (no ProfileController) | 404 expected |
| `PUT /api/v1/profile/me` | Does not exist | 404 expected |

These features exist in the frontend UI but the backend endpoints need to be added to make them fully functional.

### Quick start for a complete test run

```bash
# 1. Start Docker services
cd "C:\Users\khanm38\Desktop\new app\backend"
docker compose up -d postgres redis

# 2. Start backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

# 3. Start frontend (new terminal)
cd "C:\Users\khanm38\Desktop\new app\traveller-marketplace-frontend"
npm run dev

# 4. Open browser
# Frontend: http://localhost:5174
# Swagger: http://localhost:8080/swagger-ui/index.html
# Health:  http://localhost:8080/actuator/health
```

---

*End of Traveller-Buyer Marketplace — End-to-End Flow & Testing Guide*  
*Generated from actual project source code — frontend routes, backend controllers, and database schema.*
