import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Plane, Package, FileText, PlusCircle, ArrowRight, FileCheck } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { travellerTripApi } from "@/services/travellerTripApi";
import { bookingApi } from "@/services/bookingApi";
import { offerApi } from "@/services/offerApi";
import { kycApi } from "@/services/kycApi";
import { formatDate, formatMoney } from "@/utils/formatters";

export function TravellerDashboardPage() {
  const { user } = useAuth();

  const { data: tripsData, isLoading: tripsLoading } = useQuery({
    queryKey: ["my-trips-summary"],
    queryFn: () => travellerTripApi.my({ size: 5 }).then((r) => r.data.data),
  });

  const { data: bookingsData, isLoading: bookLoading } = useQuery({
    queryKey: ["my-bookings-summary"],
    queryFn: () => bookingApi.my(undefined, 0, 5).then((r) => r.data.data),
  });

  const { data: offersData, isLoading: offerLoading } = useQuery({
    queryKey: ["my-offers-summary"],
    queryFn: () => offerApi.my(undefined, 0, 5).then((r) => r.data.data),
  });

  const { data: kycData } = useQuery({
    queryKey: ["my-kyc"],
    queryFn: () => kycApi.getMyStatus().then((r) => r.data.data).catch(() => null),
  });

  if (tripsLoading || bookLoading || offerLoading) return <PageLoader />;

  const stats = [
    { label: "My Trips", value: tripsData?.totalElements ?? 0, icon: Plane, color: "bg-blue-50 text-blue-600", to: "/traveller/trips" },
    { label: "Bookings", value: bookingsData?.totalElements ?? 0, icon: Package, color: "bg-emerald-50 text-emerald-600", to: "/traveller/bookings" },
    { label: "Offers Sent", value: offersData?.totalElements ?? 0, icon: FileText, color: "bg-violet-50 text-violet-600", to: "/shared/chat" },
  ];

  const kycStatus = kycData?.status;

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div className="rounded-2xl bg-gradient-to-r from-emerald-600 to-teal-500 p-6 text-white shadow-lg">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-bold">Welcome, {user?.fullName?.split(" ")[0]}! ✈️</h1>
            <p className="mt-1 text-sm text-white/80">Ready to carry items and earn rewards?</p>
          </div>
          <Link to="/traveller/trips/create">
            <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />}>
              New Trip
            </Button>
          </Link>
        </div>
      </div>

      {/* KYC alert */}
      {(!kycStatus || kycStatus === "PENDING_REVIEW" || kycStatus === "REJECTED") && (
        <div className={`flex items-center justify-between rounded-xl border p-4 ${kycStatus === "REJECTED" ? "border-red-200 bg-red-50" : "border-amber-200 bg-amber-50"}`}>
          <div className="flex items-center gap-3">
            <FileCheck size={20} className={kycStatus === "REJECTED" ? "text-red-600" : "text-amber-600"} />
            <div>
              <p className="text-sm font-medium text-gray-800">
                {kycStatus === "REJECTED" ? "KYC Rejected" : kycStatus === "PENDING_REVIEW" ? "KYC Pending Review" : "KYC Verification Required"}
              </p>
              <p className="text-xs text-gray-500">
                {kycStatus === "REJECTED" ? "Resubmit your KYC documents." : kycStatus === "PENDING_REVIEW" ? "Your KYC is under review." : "Submit KYC to publish trips and send offers."}
              </p>
            </div>
          </div>
          {kycStatus !== "PENDING_REVIEW" && (
            <Link to="/traveller/kyc">
              <Button size="sm" variant={kycStatus === "REJECTED" ? "danger" : "primary"}>Submit KYC</Button>
            </Link>
          )}
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {stats.map((s) => (
          <Link key={s.label} to={s.to}>
            <Card hoverable className="flex items-center gap-4">
              <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${s.color}`}>
                <s.icon size={22} />
              </div>
              <div>
                <p className="text-2xl font-bold text-gray-900">{s.value}</p>
                <p className="text-sm text-gray-500">{s.label}</p>
              </div>
            </Card>
          </Link>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent trips */}
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">My Trips</h2>
            <Link to="/traveller/trips" className="flex items-center gap-1 text-xs text-primary-600 hover:underline">
              View all <ArrowRight size={12} />
            </Link>
          </div>
          {tripsData?.content?.length === 0 ? (
            <p className="text-sm text-gray-400">No trips yet.</p>
          ) : (
            <ul className="space-y-2">
              {tripsData?.content?.slice(0, 4).map((t) => (
                <li key={t.id}>
                  <Link
                    to={`/traveller/trips/${t.id}`}
                    className="flex items-center justify-between rounded-lg px-3 py-2 hover:bg-gray-50"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-800">
                        {t.sourceCountry} → {t.destinationCountry}
                      </p>
                      <p className="text-xs text-gray-400">{formatDate(t.travelDate)}</p>
                    </div>
                    <StatusBadge status={t.status} size="sm" />
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Recent bookings */}
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">My Bookings</h2>
            <Link to="/traveller/bookings" className="flex items-center gap-1 text-xs text-primary-600 hover:underline">
              View all <ArrowRight size={12} />
            </Link>
          </div>
          {bookingsData?.content?.length === 0 ? (
            <p className="text-sm text-gray-400">No bookings yet.</p>
          ) : (
            <ul className="space-y-2">
              {bookingsData?.content?.slice(0, 4).map((b) => (
                <li key={b.id}>
                  <Link
                    to={`/shared/bookings/${b.id}`}
                    className="flex items-center justify-between rounded-lg px-3 py-2 hover:bg-gray-50"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-800">{b.buyerRequestTitle}</p>
                      <p className="text-xs text-gray-400">Earn: {formatMoney(b.travellerFee, b.currency)}</p>
                    </div>
                    <StatusBadge status={b.status} size="sm" />
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
