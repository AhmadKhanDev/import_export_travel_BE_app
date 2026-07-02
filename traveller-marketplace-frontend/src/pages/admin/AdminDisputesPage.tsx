import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { disputeApi } from "@/services/disputeApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Modal } from "@/components/ui/Modal";
import { TextArea } from "@/components/ui/TextArea";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { formatDate } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { DisputeResponse } from "@/types/dispute";

export function AdminDisputesPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [resolveModal, setResolveModal] = useState<DisputeResponse | null>(null);
  const [rejectModal, setRejectModal] = useState<DisputeResponse | null>(null);
  const [resolution, setResolution] = useState("");
  const [rejectReason, setRejectReason] = useState("");
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-disputes", page, statusFilter],
    queryFn: () => disputeApi.adminList(statusFilter || undefined, page, 20).then((r) => r.data.data),
  });

  const markUnderReviewMutation = useMutation({
    mutationFn: (id: string) => disputeApi.markUnderReview(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-disputes"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const resolveMutation = useMutation({
    mutationFn: () => disputeApi.resolve(resolveModal!.id, { resolution }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-disputes"] }); setResolveModal(null); setResolution(""); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rejectMutation = useMutation({
    mutationFn: () => disputeApi.reject(rejectModal!.id, { reason: rejectReason }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-disputes"] }); setRejectModal(null); setRejectReason(""); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const columns = [
    {
      key: "reason",
      header: "Reason",
      render: (d: DisputeResponse) => (
        <div>
          <p className="font-medium text-gray-800">{d.reason.replace(/_/g, " ")}</p>
          <p className="text-xs text-gray-400">Raised by: {d.raisedByName}</p>
        </div>
      ),
    },
    { key: "description", header: "Description", render: (d: DisputeResponse) => d.description.slice(0, 60) + (d.description.length > 60 ? "…" : "") },
    { key: "status", header: "Status", render: (d: DisputeResponse) => <StatusBadge status={d.status} /> },
    { key: "createdAt", header: "Filed", render: (d: DisputeResponse) => formatDate(d.createdAt) },
    {
      key: "actions",
      header: "",
      render: (d: DisputeResponse) => (
        <div className="flex gap-1.5">
          {d.status === "OPEN" && (
            <Button size="sm" variant="secondary" onClick={(e) => { e.stopPropagation(); markUnderReviewMutation.mutate(d.id); }} loading={markUnderReviewMutation.isPending}>
              Review
            </Button>
          )}
          {["OPEN", "UNDER_REVIEW"].includes(d.status) && (
            <>
              <Button size="sm" onClick={(e) => { e.stopPropagation(); setResolveModal(d); }}>Resolve</Button>
              <Button size="sm" variant="danger" onClick={(e) => { e.stopPropagation(); setRejectModal(d); }}>Reject</Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Disputes</h1>
        <p className="text-sm text-gray-500">Review and resolve disputes</p>
      </div>

      <div className="flex gap-2">
        {["", "OPEN", "UNDER_REVIEW", "RESOLVED", "REJECTED"].map((s) => (
          <button key={s || "all"} onClick={() => { setStatusFilter(s); setPage(0); }}
            className={`rounded-lg px-3 py-1.5 text-xs font-medium ${statusFilter === s ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            {s || "All"}
          </button>
        ))}
      </div>

      {error && <ErrorMessage message={error} />}

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(d) => d.id} isLoading={isLoading} emptyText="No disputes." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      <Modal open={!!resolveModal} onClose={() => setResolveModal(null)} title="Resolve Dispute" size="sm">
        <div className="space-y-4">
          <TextArea label="Resolution" placeholder="Describe how this was resolved..." value={resolution} onChange={(e) => setResolution(e.target.value)} />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setResolveModal(null)}>Cancel</Button>
            <Button size="sm" onClick={() => resolveMutation.mutate()} loading={resolveMutation.isPending} disabled={!resolution.trim()}>Resolve</Button>
          </div>
        </div>
      </Modal>

      <Modal open={!!rejectModal} onClose={() => setRejectModal(null)} title="Reject Dispute" size="sm">
        <div className="space-y-4">
          <TextArea label="Reason for rejection" placeholder="..." value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setRejectModal(null)}>Cancel</Button>
            <Button variant="danger" size="sm" onClick={() => rejectMutation.mutate()} loading={rejectMutation.isPending} disabled={!rejectReason.trim()}>Reject</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
