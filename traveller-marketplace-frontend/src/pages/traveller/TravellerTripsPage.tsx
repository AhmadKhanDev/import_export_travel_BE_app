import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { PlusCircle, MapPin, Calendar } from "lucide-react";
import { travellerTripApi } from "@/services/travellerTripApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { formatDate } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { TravellerTripResponse } from "@/types/listing";

export function TravellerTripsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [confirmId, setConfirmId] = useState<{ id: string; action: "publish" | "cancel" | "delete" } | null>(null);
  const [actionError, setActionError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["my-trips", page],
    queryFn: () => travellerTripApi.my({ page, size: 20 }).then((r) => r.data.data),
  });

  const publishMutation = useMutation({
    mutationFn: (id: string) => travellerTripApi.publish(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-trips"] }); setConfirmId(null); },
    onError: (err) => setActionError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: string) => travellerTripApi.cancel(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-trips"] }); setConfirmId(null); },
    onError: (err) => setActionError(getErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => travellerTripApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["my-trips"] }); setConfirmId(null); },
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
      key: "route",
      header: "Route",
      render: (t: TravellerTripResponse) => (
        <div>
          <p className="flex items-center gap-1 font-medium text-gray-800">
            <MapPin size={12} className="text-gray-400" />
            {t.sourceCity}, {t.sourceCountry} → {t.destinationCity}, {t.destinationCountry}
          </p>
          <p className="mt-0.5 flex items-center gap-1 text-xs text-gray-400">
            <Calendar size={11} /> {formatDate(t.travelDate)}
          </p>
        </div>
      ),
    },
    {
      key: "availableCapacityKg",
      header: "Capacity",
      render: (t: TravellerTripResponse) =>
        t.availableCapacityKg ? `${t.availableCapacityKg} kg` : "—",
    },
    { key: "status", header: "Status", render: (t: TravellerTripResponse) => <StatusBadge status={t.status} /> },
    { key: "createdAt", header: "Created", render: (t: TravellerTripResponse) => formatDate(t.createdAt) },
    {
      key: "actions",
      header: "",
      render: (t: TravellerTripResponse) => (
        <div className="flex gap-1.5">
          <Link to={`/traveller/trips/${t.id}`}>
            <Button size="sm" variant="ghost" onClick={(e) => e.stopPropagation()}>View</Button>
          </Link>
          {t.status === "DRAFT" && (
            <Button size="sm" variant="outline" onClick={(e) => { e.stopPropagation(); setConfirmId({ id: t.id, action: "publish" }); }}>
              Publish
            </Button>
          )}
          {t.status === "DRAFT" && (
            <Button size="sm" variant="ghost" onClick={(e) => { e.stopPropagation(); setConfirmId({ id: t.id, action: "delete" }); }} className="text-red-500">
              Delete
            </Button>
          )}
          {["PUBLISHED", "MATCHED"].includes(t.status) && (
            <Button size="sm" variant="ghost" onClick={(e) => { e.stopPropagation(); setConfirmId({ id: t.id, action: "cancel" }); }} className="text-red-500">
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
          <h1 className="text-xl font-bold text-gray-900">My Trips</h1>
          <p className="text-sm text-gray-500">Manage your travel listings</p>
        </div>
        <Link to="/traveller/trips/create">
          <Button icon={<PlusCircle size={15} />}>New Trip</Button>
        </Link>
      </div>

      {actionError && <ErrorMessage message={actionError} />}

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(t) => t.id} isLoading={isLoading} emptyText="No trips yet." />

      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      <ConfirmDialog
        open={!!confirmId}
        onClose={() => setConfirmId(null)}
        onConfirm={handleConfirm}
        isLoading={isMutating}
        title={confirmId?.action === "publish" ? "Publish trip" : confirmId?.action === "cancel" ? "Cancel trip" : "Delete trip"}
        message={confirmId?.action === "publish" ? "This will make your trip visible to buyers. KYC must be approved." : "Are you sure?"}
        confirmLabel={confirmId?.action === "publish" ? "Publish" : confirmId?.action === "cancel" ? "Cancel trip" : "Delete"}
        variant={confirmId?.action === "publish" ? "primary" : "danger"}
      />
    </div>
  );
}
