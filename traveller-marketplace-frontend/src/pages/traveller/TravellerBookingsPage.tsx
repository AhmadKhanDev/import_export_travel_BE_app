import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { bookingApi } from "@/services/bookingApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { BookingResponse } from "@/types/booking";

export function TravellerBookingsPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["my-bookings", page],
    queryFn: () => bookingApi.my(undefined, page, 20).then((r) => r.data.data),
  });

  const markInTransitMutation = useMutation({
    mutationFn: (id: string) => bookingApi.markInTransit(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-bookings"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const markDeliveredMutation = useMutation({
    mutationFn: (id: string) => bookingApi.markDelivered(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-bookings"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const columns = [
    {
      key: "buyerRequestTitle",
      header: "Request",
      render: (b: BookingResponse) => (
        <div>
          <p className="font-medium text-gray-800">{b.buyerRequestTitle}</p>
          <p className="text-xs text-gray-400">Buyer: {b.buyerName}</p>
        </div>
      ),
    },
    {
      key: "route",
      header: "Route",
      render: (b: BookingResponse) => `${b.sourceCountry} → ${b.destinationCountry}`,
    },
    {
      key: "travellerFee",
      header: "You Earn",
      render: (b: BookingResponse) => (
        <span className="font-semibold text-emerald-700">{formatMoney(b.travellerFee, b.currency)}</span>
      ),
    },
    { key: "status", header: "Status", render: (b: BookingResponse) => <StatusBadge status={b.status} /> },
    { key: "createdAt", header: "Created", render: (b: BookingResponse) => formatDate(b.createdAt) },
    {
      key: "actions",
      header: "",
      render: (b: BookingResponse) => (
        <div className="flex gap-1.5">
          {b.status === "PAYMENT_HELD" && (
            <Button size="sm" variant="outline" onClick={(e) => { e.stopPropagation(); markInTransitMutation.mutate(b.id); }} loading={markInTransitMutation.isPending}>
              Mark In Transit
            </Button>
          )}
          {b.status === "IN_TRANSIT" && (
            <Button size="sm" variant="outline" onClick={(e) => { e.stopPropagation(); markDeliveredMutation.mutate(b.id); }} loading={markDeliveredMutation.isPending}>
              Mark Delivered
            </Button>
          )}
          {b.status === "DELIVERED_PENDING_VERIFICATION" && (
            <Button size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/shared/delivery/${b.id}`); }}>
              Verify Code
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Bookings</h1>
        <p className="text-sm text-gray-500">Track deliveries and earnings</p>
      </div>

      {error && <ErrorMessage message={error} />}

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(b) => b.id} onRowClick={(b) => navigate(`/shared/bookings/${b.id}`)} isLoading={isLoading} emptyText="No bookings yet." />

      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />
    </div>
  );
}
