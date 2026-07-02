import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { kycApi } from "@/services/kycApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { AdminKycResponse } from "@/types/user";

export function AdminKycPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("PENDING_REVIEW");
  const [rejectModal, setRejectModal] = useState<AdminKycResponse | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-kyc", page, statusFilter],
    queryFn: () => kycApi.listAll(statusFilter || undefined, page, 20).then((r) => r.data.data),
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => kycApi.approve(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-kyc"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rejectMutation = useMutation({
    mutationFn: () => kycApi.reject(rejectModal!.id, rejectReason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-kyc"] }); setRejectModal(null); setRejectReason(""); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const columns = [
    {
      key: "userFullName",
      header: "User",
      render: (k: AdminKycResponse) => (
        <div>
          <p className="font-medium text-gray-800">{k.userFullName}</p>
          <p className="text-xs text-gray-400">{k.userEmail}</p>
        </div>
      ),
    },
    { key: "status", header: "Status", render: (k: AdminKycResponse) => <StatusBadge status={k.status} /> },
    {
      key: "actions",
      header: "",
      render: (k: AdminKycResponse) =>
        k.status === "PENDING_REVIEW" ? (
          <div className="flex gap-1.5">
            <Button size="sm" onClick={(e) => { e.stopPropagation(); setError(""); approveMutation.mutate(k.id); }} loading={approveMutation.isPending}>
              Approve
            </Button>
            <Button size="sm" variant="danger" onClick={(e) => { e.stopPropagation(); setError(""); setRejectModal(k); }}>
              Reject
            </Button>
          </div>
        ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">KYC Review</h1>
        <p className="text-sm text-gray-500">Approve or reject traveller identity documents</p>
      </div>

      <div className="flex gap-3">
        {["PENDING_REVIEW", "APPROVED", "REJECTED", ""].map((s) => (
          <button
            key={s || "all"}
            onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}
          >
            {s || "All"}
          </button>
        ))}
      </div>

      {error && <ErrorMessage message={error} />}

      <Table columns={columns} data={(data as { content?: AdminKycResponse[] })?.content ?? []} keyExtractor={(k: AdminKycResponse) => k.id} isLoading={isLoading} emptyText="No KYC documents." />

      <Pagination page={page} totalPages={(data as { totalPages?: number })?.totalPages ?? 0} totalElements={(data as { totalElements?: number })?.totalElements ?? 0} size={20} onPageChange={setPage} />

      <Modal open={!!rejectModal} onClose={() => setRejectModal(null)} title="Reject KYC" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-gray-600">Rejecting KYC for: <strong>{rejectModal?.userFullName}</strong></p>
          <Input label="Reason for rejection" placeholder="e.g. Document unclear, expired..." value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setRejectModal(null)}>Cancel</Button>
            <Button variant="danger" size="sm" onClick={() => rejectMutation.mutate()} loading={rejectMutation.isPending} disabled={!rejectReason.trim()}>Reject</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
