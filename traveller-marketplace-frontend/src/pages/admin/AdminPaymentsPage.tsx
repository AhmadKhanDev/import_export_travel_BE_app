import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/services/adminApi";
import { paymentApi } from "@/services/paymentApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { PaymentResponse } from "@/types/payment";

export function AdminPaymentsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [confirmAction, setConfirmAction] = useState<{ id: string; action: "release" | "refund" } | null>(null);
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-payments", page, statusFilter],
    queryFn: () => adminApi.getPayments(statusFilter || undefined, page, 20).then((r) => r.data.data),
  });

  const releaseMutation = useMutation({
    mutationFn: (id: string) => paymentApi.release(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-payments"] }); setConfirmAction(null); },
    onError: (err) => { setError(getErrorMessage(err)); setConfirmAction(null); },
  });

  const refundMutation = useMutation({
    mutationFn: (id: string) => paymentApi.refund(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-payments"] }); setConfirmAction(null); },
    onError: (err) => { setError(getErrorMessage(err)); setConfirmAction(null); },
  });

  const columns = [
    {
      key: "id",
      header: "Payment",
      render: (p: PaymentResponse) => (
        <div>
          <p className="font-mono text-xs text-gray-600">{p.id.slice(0, 12)}…</p>
          <p className="text-xs text-gray-400">Booking: {p.bookingId.slice(0, 8)}…</p>
        </div>
      ),
    },
    { key: "amount", header: "Amount", render: (p: PaymentResponse) => formatMoney(p.amount, p.currency) },
    { key: "status", header: "Status", render: (p: PaymentResponse) => <StatusBadge status={p.status} /> },
    { key: "createdAt", header: "Date", render: (p: PaymentResponse) => formatDate(p.createdAt) },
    {
      key: "actions",
      header: "",
      render: (p: PaymentResponse) => (
        <div className="flex gap-1.5">
          {p.status === "HELD" && (
            <>
              <Button size="sm" onClick={(e) => { e.stopPropagation(); setError(""); setConfirmAction({ id: p.id, action: "release" }); }}>Release</Button>
              <Button size="sm" variant="danger" onClick={(e) => { e.stopPropagation(); setError(""); setConfirmAction({ id: p.id, action: "refund" }); }}>Refund</Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Payments</h1>
        <p className="text-sm text-gray-500">Manage escrow payments</p>
      </div>

      <div className="flex gap-2">
        {["", "PENDING", "HELD", "RELEASED", "REFUNDED"].map((s) => (
          <button key={s || "all"} onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            {s || "All"}
          </button>
        ))}
      </div>

      {error && <ErrorMessage message={error} />}

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(p) => p.id} isLoading={isLoading} emptyText="No payments found." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      <ConfirmDialog
        open={!!confirmAction}
        onClose={() => setConfirmAction(null)}
        onConfirm={() => {
          if (confirmAction?.action === "release") releaseMutation.mutate(confirmAction.id);
          else if (confirmAction?.action === "refund") refundMutation.mutate(confirmAction.id);
        }}
        isLoading={releaseMutation.isPending || refundMutation.isPending}
        title={confirmAction?.action === "release" ? "Release Payment" : "Refund Payment"}
        message={confirmAction?.action === "release" ? "Release funds to the traveller?" : "Refund funds to the buyer?"}
        confirmLabel={confirmAction?.action === "release" ? "Release" : "Refund"}
        variant={confirmAction?.action === "refund" ? "danger" : "primary"}
      />
    </div>
  );
}
