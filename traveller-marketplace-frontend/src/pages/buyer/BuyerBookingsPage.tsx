import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { bookingApi } from "@/services/bookingApi";
import { Table } from "@/components/ui/Table";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import type { BookingResponse } from "@/types/booking";

export function BuyerBookingsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ["my-bookings", page],
    queryFn: () => bookingApi.my(undefined, page, 20).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "buyerRequestTitle",
      header: "Request",
      render: (b: BookingResponse) => (
        <div>
          <p className="font-medium text-gray-800">{b.buyerRequestTitle}</p>
          <p className="text-xs text-gray-400">Traveller: {b.travellerName}</p>
        </div>
      ),
    },
    {
      key: "route",
      header: "Route",
      render: (b: BookingResponse) => `${b.sourceCountry} → ${b.destinationCountry}`,
    },
    {
      key: "totalAmount",
      header: "Total",
      render: (b: BookingResponse) => (
        <span className="font-semibold">{formatMoney(b.totalAmount, b.currency)}</span>
      ),
    },
    {
      key: "status",
      header: "Status",
      render: (b: BookingResponse) => <StatusBadge status={b.status} />,
    },
    { key: "createdAt", header: "Created", render: (b: BookingResponse) => formatDate(b.createdAt) },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Bookings</h1>
        <p className="text-sm text-gray-500">Track your active and past bookings</p>
      </div>

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(b) => b.id}
        onRowClick={(b) => navigate(`/shared/bookings/${b.id}`)}
        emptyText="No bookings yet."
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        size={20}
        onPageChange={setPage}
      />
    </div>
  );
}
