import { useQuery } from "@tanstack/react-query";
import { adminApi } from "@/services/adminApi";
import { Card } from "@/components/ui/Card";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDateTime } from "@/utils/formatters";
import {
  Users, ShoppingBag, Plane, Package, CreditCard,
  FileCheck, AlertTriangle, MessageSquare, Star, GitMerge,
} from "lucide-react";

export function AdminDashboardPage() {
  const { data: summary, isLoading } = useQuery({
    queryKey: ["admin-dashboard-summary"],
    queryFn: () => adminApi.getDashboardSummary().then((r) => r.data.data),
  });

  const { data: recentActivity } = useQuery({
    queryKey: ["admin-recent-activity"],
    queryFn: () => adminApi.getRecentActivity(0, 8).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;
  if (!summary) return null;

  const statCards = [
    { label: "Total Users", value: summary.totalUsers, sub: `${summary.activeUsers} active`, icon: Users, color: "text-blue-600 bg-blue-50" },
    { label: "Buyer Requests", value: summary.publishedBuyerRequests, sub: "published", icon: ShoppingBag, color: "text-violet-600 bg-violet-50" },
    { label: "Traveller Trips", value: summary.publishedTravellerTrips, sub: "published", icon: Plane, color: "text-teal-600 bg-teal-50" },
    { label: "Total Bookings", value: summary.totalBookings, sub: `${summary.completedBookings} completed`, icon: Package, color: "text-emerald-600 bg-emerald-50" },
    { label: "Held Payments", value: summary.heldPayments, sub: `${summary.releasedPayments} released`, icon: CreditCard, color: "text-amber-600 bg-amber-50" },
    { label: "Pending KYC", value: summary.pendingKycCount, sub: `${summary.approvedKycCount} approved`, icon: FileCheck, color: "text-orange-600 bg-orange-50" },
    { label: "Open Disputes", value: summary.openDisputes, sub: `${summary.underReviewDisputes} under review`, icon: AlertTriangle, color: "text-red-600 bg-red-50" },
    { label: "Total Matches", value: summary.totalMatches, sub: `${summary.totalOffers} offers`, icon: GitMerge, color: "text-indigo-600 bg-indigo-50" },
    { label: "Total Reviews", value: summary.totalReviews, sub: "", icon: Star, color: "text-yellow-600 bg-yellow-50" },
    { label: "Chat Rooms", value: summary.totalChatRooms, sub: "", icon: MessageSquare, color: "text-pink-600 bg-pink-50" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-sm text-gray-500">Platform overview and statistics</p>
      </div>

      {/* User breakdown */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: "Total Users", value: summary.totalUsers },
          { label: "Buyers", value: summary.totalBuyers },
          { label: "Travellers", value: summary.totalTravellers },
          { label: "Admins", value: summary.totalAdmins },
        ].map((s) => (
          <Card key={s.label} className="text-center">
            <p className="text-3xl font-bold text-gray-900">{s.value}</p>
            <p className="text-xs text-gray-500 mt-1">{s.label}</p>
          </Card>
        ))}
      </div>

      {/* Main stats grid */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {statCards.map((s) => (
          <Card key={s.label} className="flex flex-col gap-2">
            <div className={`flex h-9 w-9 items-center justify-center rounded-xl ${s.color}`}>
              <s.icon size={18} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-900">{s.value}</p>
              <p className="text-xs font-medium text-gray-600">{s.label}</p>
              {s.sub && <p className="text-xs text-gray-400">{s.sub}</p>}
            </div>
          </Card>
        ))}
      </div>

      {/* Recent activity */}
      <Card>
        <h2 className="mb-4 font-semibold text-gray-900">Recent Activity</h2>
        {recentActivity?.content?.length === 0 ? (
          <p className="text-sm text-gray-400">No recent activity.</p>
        ) : (
          <div className="space-y-2">
            {recentActivity?.content?.map((a) => (
              <div key={a.id} className="flex items-start gap-3 rounded-lg bg-gray-50 px-3 py-2">
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-800">{a.action.replace(/_/g, " ")}</p>
                  <p className="text-xs text-gray-500">
                    {a.entityType} · {a.actorEmail ?? "System"}
                    {a.details ? ` · ${a.details}` : ""}
                  </p>
                </div>
                <p className="shrink-0 text-xs text-gray-400">{formatDateTime(a.createdAt)}</p>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
