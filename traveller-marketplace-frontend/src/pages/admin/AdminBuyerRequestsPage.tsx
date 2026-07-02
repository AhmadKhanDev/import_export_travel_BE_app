import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { adminApi } from "@/services/adminApi";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { Table } from "@/components/ui/Table";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import type { BuyerRequestResponse } from "@/types/listing";

export function AdminBuyerRequestsPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-buyer-requests", page, statusFilter],
    queryFn: () =>
      buyerRequestApi.search({ status: statusFilter as BuyerRequestResponse["status"] | undefined || undefined, page, size: 20 }).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "title",
      header: "Title",
      render: (r: BuyerRequestResponse) => (
        <div>
          <p className="font-medium text-gray-800">{r.title}</p>
          <p className="text-xs text-gray-400">{r.buyerName}</p>
        </div>
      ),
    },
    { key: "route", header: "Route", render: (r: BuyerRequestResponse) => `${r.sourceCountry} → ${r.destinationCountry}` },
    { key: "itemCategory", header: "Category" },
    { key: "travellersReward", header: "Reward", render: (r: BuyerRequestResponse) => formatMoney(r.travellersReward, r.currency) },
    { key: "status", header: "Status", render: (r: BuyerRequestResponse) => <StatusBadge status={r.status} /> },
    { key: "createdAt", header: "Created", render: (r: BuyerRequestResponse) => formatDate(r.createdAt) },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Buyer Requests</h1>
        <p className="text-sm text-gray-500">All buyer requests on the platform</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {["", "DRAFT", "PUBLISHED", "MATCHED", "BOOKED", "COMPLETED", "CANCELLED"].map((s) => (
          <button key={s || "all"} onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            {s || "All"}
          </button>
        ))}
      </div>

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(r) => r.id} emptyText="No buyer requests." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />
    </div>
  );
}
