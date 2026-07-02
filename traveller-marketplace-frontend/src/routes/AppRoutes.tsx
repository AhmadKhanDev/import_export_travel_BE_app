import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import { RoleRoute } from "@/auth/RoleRoute";

// Layouts
import { AppLayout } from "@/components/layout/AppLayout";
import { AuthLayout } from "@/components/layout/AuthLayout";

// Auth pages
import { LoginPage } from "@/pages/auth/LoginPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";

// Buyer pages
import { BuyerDashboardPage } from "@/pages/buyer/BuyerDashboardPage";
import { BuyerRequestsPage } from "@/pages/buyer/BuyerRequestsPage";
import { CreateBuyerRequestPage } from "@/pages/buyer/CreateBuyerRequestPage";
import { BuyerRequestDetailPage } from "@/pages/buyer/BuyerRequestDetailPage";
import { BuyerMatchesPage } from "@/pages/buyer/BuyerMatchesPage";
import { BuyerOffersPage } from "@/pages/buyer/BuyerOffersPage";
import { BuyerBookingsPage } from "@/pages/buyer/BuyerBookingsPage";

// Traveller pages
import { TravellerDashboardPage } from "@/pages/traveller/TravellerDashboardPage";
import { KycPage } from "@/pages/traveller/KycPage";
import { TravellerTripsPage } from "@/pages/traveller/TravellerTripsPage";
import { CreateTravellerTripPage } from "@/pages/traveller/CreateTravellerTripPage";
import { TravellerTripDetailPage } from "@/pages/traveller/TravellerTripDetailPage";
import { TravellerMatchesPage } from "@/pages/traveller/TravellerMatchesPage";
import { TravellerBookingsPage } from "@/pages/traveller/TravellerBookingsPage";

// Shared pages
import { ProfilePage } from "@/pages/shared/ProfilePage";
import { BookingDetailPage } from "@/pages/shared/BookingDetailPage";
import { PaymentPage } from "@/pages/shared/PaymentPage";
import { DeliveryVerificationPage } from "@/pages/shared/DeliveryVerificationPage";
import { NotificationsPage } from "@/pages/shared/NotificationsPage";
import { ReviewsPage } from "@/pages/shared/ReviewsPage";
import { DisputesPage } from "@/pages/shared/DisputesPage";
import { ChatPage } from "@/pages/shared/ChatPage";

// Admin pages
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage";
import { AdminKycPage } from "@/pages/admin/AdminKycPage";
import { AdminBuyerRequestsPage } from "@/pages/admin/AdminBuyerRequestsPage";
import { AdminTravellerTripsPage } from "@/pages/admin/AdminTravellerTripsPage";
import { AdminBookingsPage } from "@/pages/admin/AdminBookingsPage";
import { AdminPaymentsPage } from "@/pages/admin/AdminPaymentsPage";
import { AdminDisputesPage } from "@/pages/admin/AdminDisputesPage";
import { AdminNotificationsPage } from "@/pages/admin/AdminNotificationsPage";
import { AdminAuditLogsPage } from "@/pages/admin/AdminAuditLogsPage";

function RoleRedirect() {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role === "BUYER") return <Navigate to="/buyer/dashboard" replace />;
  if (user?.role === "TRAVELLER") return <Navigate to="/traveller/dashboard" replace />;
  if (user?.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />;
  return <Navigate to="/login" replace />;
}

export function AppRoutes() {
  return (
    <Routes>
      {/* Root redirect */}
      <Route path="/" element={<RoleRedirect />} />

      {/* Auth */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      {/* Protected app routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          {/* Buyer routes */}
          <Route element={<RoleRoute roles={["BUYER"]} />}>
            <Route path="/buyer/dashboard" element={<BuyerDashboardPage />} />
            <Route path="/buyer/requests" element={<BuyerRequestsPage />} />
            <Route path="/buyer/requests/create" element={<CreateBuyerRequestPage />} />
            <Route path="/buyer/requests/:id" element={<BuyerRequestDetailPage />} />
            <Route path="/buyer/matches" element={<BuyerMatchesPage />} />
            <Route path="/buyer/offers" element={<BuyerOffersPage />} />
            <Route path="/buyer/bookings" element={<BuyerBookingsPage />} />
            <Route path="/buyer/notifications" element={<NotificationsPage />} />
            <Route path="/buyer/profile" element={<ProfilePage />} />
          </Route>

          {/* Traveller routes */}
          <Route element={<RoleRoute roles={["TRAVELLER"]} />}>
            <Route path="/traveller/dashboard" element={<TravellerDashboardPage />} />
            <Route path="/traveller/kyc" element={<KycPage />} />
            <Route path="/traveller/trips" element={<TravellerTripsPage />} />
            <Route path="/traveller/trips/create" element={<CreateTravellerTripPage />} />
            <Route path="/traveller/trips/:id" element={<TravellerTripDetailPage />} />
            <Route path="/traveller/matches" element={<TravellerMatchesPage />} />
            <Route path="/traveller/bookings" element={<TravellerBookingsPage />} />
            <Route path="/traveller/notifications" element={<NotificationsPage />} />
            <Route path="/traveller/profile" element={<ProfilePage />} />
          </Route>

          {/* Admin routes */}
          <Route element={<RoleRoute roles={["ADMIN"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/kyc" element={<AdminKycPage />} />
            <Route path="/admin/buyer-requests" element={<AdminBuyerRequestsPage />} />
            <Route path="/admin/traveller-trips" element={<AdminTravellerTripsPage />} />
            <Route path="/admin/bookings" element={<AdminBookingsPage />} />
            <Route path="/admin/payments" element={<AdminPaymentsPage />} />
            <Route path="/admin/disputes" element={<AdminDisputesPage />} />
            <Route path="/admin/notifications" element={<AdminNotificationsPage />} />
            <Route path="/admin/audit-logs" element={<AdminAuditLogsPage />} />
            <Route path="/admin/profile" element={<ProfilePage />} />
          </Route>

          {/* Shared routes – accessible by both BUYER and TRAVELLER */}
          <Route element={<RoleRoute roles={["BUYER", "TRAVELLER", "ADMIN"]} />}>
            <Route path="/shared/bookings/:id" element={<BookingDetailPage />} />
            <Route path="/shared/payments/:bookingId" element={<PaymentPage />} />
            <Route path="/shared/delivery/:id" element={<DeliveryVerificationPage />} />
            <Route path="/shared/reviews" element={<ReviewsPage />} />
            <Route path="/shared/disputes" element={<DisputesPage />} />
            <Route path="/shared/chat" element={<ChatPage />} />
          </Route>
        </Route>
      </Route>

      {/* 404 fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
