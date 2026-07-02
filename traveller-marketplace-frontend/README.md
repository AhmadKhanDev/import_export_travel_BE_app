# Traveller Marketplace Frontend

A full-featured React TypeScript frontend for the Traveller-Buyer Marketplace backend.

## Tech Stack

| Tool | Purpose |
|------|---------|
| React 18 + TypeScript | UI & type safety |
| Vite | Dev server & build tool |
| Tailwind CSS | Styling |
| React Router v6 | Client-side routing |
| TanStack Query v5 | Data fetching & caching |
| Axios | HTTP client with JWT interceptor |
| React Hook Form + Zod | Form handling & validation |
| Lucide React | Icons |

## Quick Start

### 1. Prerequisites

- Node.js 18+
- Backend running at `http://localhost:8080`

### 2. Install Dependencies

```bash
cd traveller-marketplace-frontend
npm install
```

### 3. Configure Environment

The `.env` file is already pre-configured:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 4. Run Dev Server

```bash
npm run dev
```

Opens at http://localhost:5174

### 5. Build for Production

```bash
npm run build
```

---

## Features by Role

### BUYER
- Register / Login
- Create, publish, cancel buyer requests
- View matches for published requests
- Receive and accept/reject traveller offers
- Pay for bookings (simulated escrow)
- Generate delivery verification code
- Track booking progress
- Chat with traveller
- Leave reviews
- Raise disputes
- Notifications

### TRAVELLER
- Register / Login
- Submit KYC documents (admin approval required to publish)
- Create, publish, cancel trips
- Generate matches for published trips
- Send offers to buyers
- Mark bookings in transit / delivered
- Verify delivery code to release payment
- Chat with buyer
- Leave reviews
- Raise disputes
- Notifications

### ADMIN
- Dashboard with platform statistics
- Manage users (enable/disable/change role)
- Review and approve/reject KYC submissions
- View all buyer requests and traveller trips
- View all bookings
- Release or refund payments
- Resolve or reject disputes
- Send manual notifications
- View audit logs

---

## Test Accounts

Register these via the Register page or Swagger:

```
# Buyer
Email: buyer@test.com
Password: Password@123
Role: BUYER

# Traveller
Email: traveller@test.com
Password: Password@123
Role: TRAVELLER

# Admin (create via SQL)
Email: admin@test.com
Password: AdminPass@123
Role: ADMIN (update via SQL: UPDATE users SET role = 'ADMIN' WHERE email = 'admin@test.com')
```

---

## Backend Connection

The frontend connects to:
```
http://localhost:8080/api/v1
```

Make sure the backend is running with:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

And CORS allows `http://localhost:5174` in `.env`:
```
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174,http://localhost:3000
```

---

## Project Structure

```
src/
  app/           - App.tsx, QueryClient
  api/           - Axios client + error handler
  auth/          - AuthContext, ProtectedRoute, RoleRoute
  components/
    ui/           - Reusable UI components (Button, Input, Card, Table, ...)
    layout/       - AppLayout, Sidebar, Topbar, AuthLayout
    common/       - EmptyState, ErrorMessage, ConfirmDialog
  pages/
    auth/         - Login, Register
    buyer/        - 7 buyer pages
    traveller/    - 7 traveller pages
    shared/       - 8 shared pages (booking, chat, reviews, disputes, ...)
    admin/        - 10 admin pages
  routes/        - AppRoutes.tsx
  services/      - API service files (one per module)
  types/         - TypeScript type definitions
  utils/         - Formatters, constants, token storage
  main.tsx       - App entry point
  index.css      - Global styles with Tailwind
```

---

## End-to-End Testing

A complete testing guide covering every feature, screen, API call, expected result, and troubleshooting tip is available:

- **Markdown guide:** [`../docs/end-to-end-testing-guide.md`](../docs/end-to-end-testing-guide.md)
- **Word document:** [`../Traveller_Buyer_Marketplace_End_To_End_Testing_Guide.docx`](../Traveller_Buyer_Marketplace_End_To_End_Testing_Guide.docx)

The guide covers:

| Section | What it explains |
|---------|-----------------|
| Project Overview | Full business flow diagram |
| Roles | BUYER, TRAVELLER, ADMIN capabilities |
| Before Testing | Prerequisites, how to start services |
| Test Users | How to create buyer, traveller, and admin accounts |
| Happy Path | All 20 steps from register to payment release |
| Auth Testing | Register, login, logout, token refresh, role redirect |
| KYC Testing | Traveller KYC submission and admin approval |
| Buyer Requests | Create, publish, cancel, delete requests |
| Traveller Trips | Create, publish, cancel trips |
| Matching | Generate and view matches, match scores |
| Offers | Send, accept, reject offers |
| Bookings | View, cancel bookings |
| Payments | Pay, hold in escrow, release, refund |
| Delivery | Generate code, mark in-transit, verify code |
| Notifications | View, mark as read, unread count |
| Chat | Send messages, read messages, unread count |
| Reviews | Submit reviews, view rating summary |
| Disputes | Raise, admin review, resolve |
| Admin Panel | Dashboard, users, KYC, bookings, payments, disputes, audit logs |
| Status Flows | All status transitions for every entity |
| DevTools Guide | How to inspect API calls in browser |
| DB Verification | SQL queries to check data after testing |
| Checklist | 50+ checkboxes for a complete test run |
| Troubleshooting | Common errors and how to fix them |
| Test Report | Template for documenting test results |
