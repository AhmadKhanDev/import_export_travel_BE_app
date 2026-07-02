import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { adminApi, type AuditLogResponse } from "@/services/adminApi";
import { Table } from "@/components/ui/Table";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDateTime } from "@/utils/formatters";

export function AdminAuditLogsPage() {
  const [page, setPage] = useState(0);
  const [actionFilter, setActionFilter] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-audit-logs", page, actionFilter],
    queryFn: () => adminApi.getAuditLogs({ action: actionFilter || undefined }, page, 20).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "action",
      header: "Action",
      render: (a: AuditLogResponse) => (
        <span className="font-mono text-xs text-primary-700 bg-primary-50 px-2 py-0.5 rounded">
          {a.action}
        </span>
      ),
    },
    { key: "entityType", header: "Entity Type" },
    {
      key: "entityId",
      header: "Entity ID",
      render: (a: AuditLogResponse) => a.entityId ? a.entityId.slice(0, 12) + "…" : "—",
    },
    {
      key: "actorEmail",
      header: "Actor",
      render: (a: AuditLogResponse) => a.actorEmail ?? "System",
    },
    {
      key: "details",
      header: "Details",
      render: (a: AuditLogResponse) =>
        a.details ? a.details.slice(0, 50) + (a.details.length > 50 ? "…" : "") : "—",
    },
    {
      key: "createdAt",
      header: "Time",
      render: (a: AuditLogResponse) => formatDateTime(a.createdAt),
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Audit Logs</h1>
        <p className="text-sm text-gray-500">Complete audit trail of admin actions</p>
      </div>

      <div className="flex gap-3">
        <input
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-100"
          placeholder="Filter by action…"
          value={actionFilter}
          onChange={(e) => { setActionFilter(e.target.value); setPage(0); }}
        />
      </div>

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(a) => a.id} emptyText="No audit logs." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />
    </div>
  );
}
