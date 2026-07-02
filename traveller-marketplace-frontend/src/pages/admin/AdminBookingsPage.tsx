import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { adminApi } from "@/services/adminApi";
import { Table } from "@/components/ui/Table";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import type { BookingResponse } from "@/types/booking";

export function AdminBookingsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-bookings", page, statusFilter],
    queryFn: () => adminApi.getBookings(statusFilter || undefined, page, 20).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "buyerRequestTitle",
      header: "Request",
      render: (b: BookingResponse) => (
        <div>
          <p className="font-medium text-gray-800">{b.buyerRequestTitle}</p>
          <p className="text-xs text-gray-400">{b.sourceCountry} → {b.destinationCountry}</p>
        </div>
      ),
    },
    { key: "buyerName", header: "Buyer" },
    { key: "travellerName", header: "Traveller" },
    { key: "totalAmount", header: "Total", render: (b: BookingResponse) => formatMoney(b.totalAmount, b.currency) },
    { key: "status", header: "Status", render: (b: BookingResponse) => <StatusBadge status={b.status} /> },
    { key: "createdAt", header: "Created", render: (b: BookingResponse) => formatDate(b.createdAt) },
  ];

  const statuses = ["", "PAYMENT_PENDING", "PAYMENT_HELD", "IN_TRANSIT", "COMPLETED", "CANCELLED", "DISPUTED"];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">All Bookings</h1>
        <p className="text-sm text-gray-500">{data?.totalElements ?? 0} bookings</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {statuses.map((s) => (
          <button key={s || "all"} onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            {s || "All"}
          </button>
        ))}
      </div>

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(b) => b.id} onRowClick={(b) => navigate(`/shared/bookings/${b.id}`)} emptyText="No bookings found." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />
    </div>
  );
}
