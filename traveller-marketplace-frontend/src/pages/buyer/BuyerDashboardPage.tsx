import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  ShoppingBag,
  Package,
  FileText,
  PlusCircle,
  ArrowRight,
  TrendingUp,
} from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { bookingApi } from "@/services/bookingApi";
import { offerApi } from "@/services/offerApi";
import { formatMoney } from "@/utils/formatters";

export function BuyerDashboardPage() {
  const { user } = useAuth();

  const { data: requestsData, isLoading: reqLoading } = useQuery({
    queryKey: ["my-requests-summary"],
    queryFn: () => buyerRequestApi.my({ size: 5 }).then((r) => r.data.data),
  });

  const { data: bookingsData, isLoading: bookLoading } = useQuery({
    queryKey: ["my-bookings-summary"],
    queryFn: () => bookingApi.my(undefined, 0, 5).then((r) => r.data.data),
  });

  const { data: offersData, isLoading: offerLoading } = useQuery({
    queryKey: ["my-offers-summary"],
    queryFn: () => offerApi.my(undefined, 0, 5).then((r) => r.data.data),
  });

  if (reqLoading || bookLoading || offerLoading) return <PageLoader />;

  const stats = [
    {
      label: "My Requests",
      value: requestsData?.totalElements ?? 0,
      icon: ShoppingBag,
      color: "bg-blue-50 text-blue-600",
      to: "/buyer/requests",
    },
    {
      label: "Active Bookings",
      value: bookingsData?.totalElements ?? 0,
      icon: Package,
      color: "bg-emerald-50 text-emerald-600",
      to: "/buyer/bookings",
    },
    {
      label: "Pending Offers",
      value: offersData?.totalElements ?? 0,
      icon: FileText,
      color: "bg-violet-50 text-violet-600",
      to: "/buyer/offers",
    },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div className="rounded-2xl bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-white shadow-lg">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-xl font-bold">Welcome, {user?.fullName?.split(" ")[0]}! 👋</h1>
            <p className="mt-1 text-sm text-white/80">
              Manage your shopping requests and bookings from here.
            </p>
          </div>
          <Link to="/buyer/requests/create">
            <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />}>
              New Request
            </Button>
          </Link>
        </div>
      </div>

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
        {/* Recent requests */}
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Recent Requests</h2>
            <Link to="/buyer/requests" className="flex items-center gap-1 text-xs text-primary-600 hover:underline">
              View all <ArrowRight size={12} />
            </Link>
          </div>
          {requestsData?.content?.length === 0 ? (
            <p className="text-sm text-gray-400">No requests yet.</p>
          ) : (
            <ul className="space-y-2">
              {requestsData?.content?.slice(0, 4).map((req) => (
                <li key={req.id}>
                  <Link
                    to={`/buyer/requests/${req.id}`}
                    className="flex items-center justify-between rounded-lg px-3 py-2 hover:bg-gray-50"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-800">{req.title}</p>
                      <p className="text-xs text-gray-400">
                        {req.sourceCountry} → {req.destinationCountry}
                      </p>
                    </div>
                    <span className="text-xs font-medium text-emerald-600">
                      {formatMoney(req.travellersReward, req.currency)}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Recent bookings */}
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Recent Bookings</h2>
            <Link to="/buyer/bookings" className="flex items-center gap-1 text-xs text-primary-600 hover:underline">
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
                      <p className="text-xs text-gray-400">Traveller: {b.travellerName}</p>
                    </div>
                    <span className={`text-xs font-medium ${b.status === "COMPLETED" ? "text-emerald-600" : "text-amber-600"}`}>
                      {b.status.replace(/_/g, " ")}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>

      {/* Quick actions */}
      <Card>
        <h2 className="mb-4 font-semibold text-gray-900">Quick Actions</h2>
        <div className="flex flex-wrap gap-3">
          <Link to="/buyer/requests/create">
            <Button icon={<PlusCircle size={15} />} size="sm">Create Request</Button>
          </Link>
          <Link to="/buyer/requests">
            <Button variant="secondary" icon={<ShoppingBag size={15} />} size="sm">View Requests</Button>
          </Link>
          <Link to="/buyer/bookings">
            <Button variant="secondary" icon={<Package size={15} />} size="sm">My Bookings</Button>
          </Link>
          <Link to="/buyer/offers">
            <Button variant="secondary" icon={<FileText size={15} />} size="sm">View Offers</Button>
          </Link>
        </div>
      </Card>
    </div>
  );
}
