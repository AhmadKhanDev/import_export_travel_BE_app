import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { travellerTripApi } from "@/services/travellerTripApi";
import { Table } from "@/components/ui/Table";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate } from "@/utils/formatters";
import type { TravellerTripResponse } from "@/types/listing";

export function AdminTravellerTripsPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-traveller-trips", page, statusFilter],
    queryFn: () =>
      travellerTripApi.search({ status: statusFilter as TravellerTripResponse["status"] | undefined || undefined, page, size: 20 }).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "route",
      header: "Route",
      render: (t: TravellerTripResponse) => (
        <div>
          <p className="font-medium text-gray-800">{t.sourceCity}, {t.sourceCountry} → {t.destinationCity}, {t.destinationCountry}</p>
          <p className="text-xs text-gray-400">{t.travellerName}</p>
        </div>
      ),
    },
    { key: "travelDate", header: "Travel Date", render: (t: TravellerTripResponse) => formatDate(t.travelDate) },
    { key: "availableCapacityKg", header: "Capacity", render: (t: TravellerTripResponse) => t.availableCapacityKg ? `${t.availableCapacityKg} kg` : "—" },
    { key: "status", header: "Status", render: (t: TravellerTripResponse) => <StatusBadge status={t.status} /> },
    { key: "createdAt", header: "Created", render: (t: TravellerTripResponse) => formatDate(t.createdAt) },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Traveller Trips</h1>
        <p className="text-sm text-gray-500">All traveller trips on the platform</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {["", "DRAFT", "PUBLISHED", "MATCHED", "COMPLETED", "CANCELLED"].map((s) => (
          <button key={s || "all"} onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            {s || "All"}
          </button>
        ))}
      </div>

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(t) => t.id} emptyText="No traveller trips." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />
    </div>
  );
}
