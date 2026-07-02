import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { PlusCircle, MapPin, Tag, DollarSign } from "lucide-react";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { BuyerRequestResponse } from "@/types/listing";

export function BuyerRequestsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [confirmId, setConfirmId] = useState<{ id: string; action: "publish" | "cancel" | "delete" } | null>(null);
  const [actionError, setActionError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["my-requests", page],
    queryFn: () => buyerRequestApi.my({ page, size: 20 }).then((r) => r.data.data),
  });

  const publishMutation = useMutation({
    mutationFn: (id: string) => buyerRequestApi.publish(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-requests"] }); setConfirmId(null); },
    onError: (err) => setActionError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: string) => buyerRequestApi.cancel(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-requests"] }); setConfirmId(null); },
    onError: (err) => setActionError(getErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => buyerRequestApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-requests"] }); setConfirmId(null); },
    onError: (err) => setActionError(getErrorMessage(err)),
  });

  const handleConfirm = () => {
    if (!confirmId) return;
    setActionError("");
    if (confirmId.action === "publish") publishMutation.mutate(confirmId.id);
    else if (confirmId.action === "cancel") cancelMutation.mutate(confirmId.id);
    else deleteMutation.mutate(confirmId.id);
  };

  const isMutating = publishMutation.isPending || cancelMutation.isPending || deleteMutation.isPending;

  const columns = [
    {
      key: "title",
      header: "Request",
      render: (r: BuyerRequestResponse) => (
        <div>
          <Link to={`/buyer/requests/${r.id}`} className="font-medium text-primary-600 hover:underline">
            {r.title}
          </Link>
          <div className="mt-0.5 flex items-center gap-1 text-xs text-gray-400">
            <MapPin size={11} />
            {r.sourceCountry} → {r.destinationCountry}
          </div>
        </div>
      ),
    },
    {
      key: "itemCategory",
      header: "Category",
      render: (r: BuyerRequestResponse) => (
        <span className="flex items-center gap-1 text-sm">
          <Tag size={12} className="text-gray-400" /> {r.itemCategory}
        </span>
      ),
    },
    {
      key: "travellersReward",
      header: "Reward",
      render: (r: BuyerRequestResponse) => (
        <span className="flex items-center gap-1 font-medium text-emerald-700">
          <DollarSign size={12} /> {formatMoney(r.travellersReward, r.currency)}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status",
      render: (r: BuyerRequestResponse) => <StatusBadge status={r.status} />,
    },
    {
      key: "createdAt",
      header: "Created",
      render: (r: BuyerRequestResponse) => formatDate(r.createdAt),
    },
    {
      key: "actions",
      header: "",
      render: (r: BuyerRequestResponse) => (
        <div className="flex items-center gap-1.5">
          {r.status === "DRAFT" && (
            <Button
              size="sm"
              variant="outline"
              onClick={(e) => { e.stopPropagation(); setActionError(""); setConfirmId({ id: r.id, action: "publish" }); }}
            >
              Publish
            </Button>
          )}
          {r.status === "DRAFT" && (
            <Button
              size="sm"
              variant="ghost"
              onClick={(e) => { e.stopPropagation(); setActionError(""); setConfirmId({ id: r.id, action: "delete" }); }}
              className="text-red-500 hover:bg-red-50"
            >
              Delete
            </Button>
          )}
          {["PUBLISHED", "MATCHED"].includes(r.status) && (
            <Button
              size="sm"
              variant="ghost"
              onClick={(e) => { e.stopPropagation(); setActionError(""); setConfirmId({ id: r.id, action: "cancel" }); }}
              className="text-red-500 hover:bg-red-50"
            >
              Cancel
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">My Requests</h1>
          <p className="text-sm text-gray-500">Manage your buyer requests</p>
        </div>
        <Link to="/buyer/requests/create">
          <Button icon={<PlusCircle size={15} />}>New Request</Button>
        </Link>
      </div>

      {actionError && <ErrorMessage message={actionError} />}

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(r) => r.id}
        isLoading={isLoading}
        emptyText="No requests yet. Create your first one!"
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        size={20}
        onPageChange={setPage}
      />

      <ConfirmDialog
        open={!!confirmId}
        onClose={() => setConfirmId(null)}
        onConfirm={handleConfirm}
        isLoading={isMutating}
        title={
          confirmId?.action === "publish"
            ? "Publish request"
            : confirmId?.action === "cancel"
              ? "Cancel request"
              : "Delete request"
        }
        message={
          confirmId?.action === "publish"
            ? "This will make your request visible to travellers."
            : confirmId?.action === "cancel"
              ? "This will cancel your request."
              : "This will permanently delete your draft request."
        }
        confirmLabel={
          confirmId?.action === "publish" ? "Publish" : confirmId?.action === "cancel" ? "Cancel request" : "Delete"
        }
        variant={confirmId?.action === "publish" ? "primary" : "danger"}
      />
    </div>
  );
}
