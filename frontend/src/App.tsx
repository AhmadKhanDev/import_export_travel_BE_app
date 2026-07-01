import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/store/AuthContext";
import { AppLayout } from "@/components/layout/AppLayout";
import { ProtectedRoute } from "@/components/layout/ProtectedRoute";
import { HomePage } from "@/pages/HomePage";
import { LoginPage } from "@/pages/LoginPage";
import { RegisterPage } from "@/pages/RegisterPage";
import { DashboardPage } from "@/pages/DashboardPage";
import { BrowsePage } from "@/pages/BrowsePage";
import { BuyerRequestsPage } from "@/pages/buyer/BuyerRequestsPage";
import { CreateBuyerRequestPage } from "@/pages/buyer/CreateBuyerRequestPage";
import { BuyerRequestDetailPage } from "@/pages/buyer/BuyerRequestDetailPage";
import { TravellerTripsPage } from "@/pages/traveller/TravellerTripsPage";
import { CreateTravellerTripPage } from "@/pages/traveller/CreateTravellerTripPage";
import { TravellerTripDetailPage } from "@/pages/traveller/TravellerTripDetailPage";
import { OffersPage } from "@/pages/OffersPage";
import { BookingsPage } from "@/pages/BookingsPage";
import { BookingDetailPage } from "@/pages/BookingDetailPage";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1 },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<AppLayout />}>
              <Route index element={<HomePage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
              <Route element={<ProtectedRoute />}>
                <Route path="browse" element={<BrowsePage />} />
                <Route path="dashboard" element={<DashboardPage />} />
                <Route path="offers" element={<OffersPage />} />
                <Route path="bookings" element={<BookingsPage />} />
                <Route path="bookings/:id" element={<BookingDetailPage />} />
                <Route path="buyer/requests/:id" element={<BuyerRequestDetailPage />} />
                <Route path="traveller/trips/:id" element={<TravellerTripDetailPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={["BUYER"]} />}>
                <Route path="buyer/requests" element={<BuyerRequestsPage />} />
                <Route path="buyer/requests/new" element={<CreateBuyerRequestPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={["TRAVELLER"]} />}>
                <Route path="traveller/trips" element={<TravellerTripsPage />} />
                <Route path="traveller/trips/new" element={<CreateTravellerTripPage />} />
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
