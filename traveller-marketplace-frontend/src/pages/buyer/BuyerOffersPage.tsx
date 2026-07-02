import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { offerApi } from "@/services/offerApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { OfferResponse } from "@/types/offer";

export function BuyerOffersPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [confirmAccept, setConfirmAccept] = useState<string | null>(null);
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["my-offers", page],
    queryFn: () => offerApi.my(undefined, page, 20).then((r) => r.data.data),
  });

  const acceptMutation = useMutation({
    mutationFn: (offerId: string) => offerApi.accept(offerId),
    onSuccess: (res) => {
      setConfirmAccept(null);
      navigate(`/shared/bookings/${res.data.data.id}`);
    },
    onError: (err) => { setError(getErrorMessage(err)); setConfirmAccept(null); },
  });

  const rejectMutation = useMutation({
    mutationFn: (offerId: string) => offerApi.reject(offerId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-offers"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <PageLoader />;

  const columns = [
    {
      key: "buyerRequestTitle",
      header: "Request",
      render: (o: OfferResponse) => (
        <div>
          <p className="font-medium text-gray-800">{o.buyerRequestTitle}</p>
          <p className="text-xs text-gray-400">From: {o.travellerName}</p>
        </div>
      ),
    },
    {
      key: "travellerFee",
      header: "Traveller Fee",
      render: (o: OfferResponse) => formatMoney(o.travellerFee, o.currency),
    },
    {
      key: "totalAmount",
      header: "Total",
      render: (o: OfferResponse) => (
        <span className="font-semibold text-gray-900">{formatMoney(o.totalAmount, o.currency)}</span>
      ),
    },
    {
      key: "status",
      header: "Status",
      render: (o: OfferResponse) => <StatusBadge status={o.status} />,
    },
    {
      key: "createdAt",
      header: "Received",
      render: (o: OfferResponse) => formatDate(o.createdAt),
    },
    {
      key: "actions",
      header: "",
      render: (o: OfferResponse) =>
        o.status === "SENT" ? (
          <div className="flex gap-1.5">
            <Button size="sm" onClick={(e) => { e.stopPropagation(); setError(""); setConfirmAccept(o.id); }}>
              Accept
            </Button>
            <Button
              size="sm"
              variant="danger"
              onClick={(e) => { e.stopPropagation(); setError(""); rejectMutation.mutate(o.id); }}
              loading={rejectMutation.isPending}
            >
              Reject
            </Button>
          </div>
        ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Offers</h1>
        <p className="text-sm text-gray-500">Offers received from travellers</p>
      </div>

      {error && <ErrorMessage message={error} />}

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(o) => o.id}
        emptyText="No offers received yet."
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        size={20}
        onPageChange={setPage}
      />

      <ConfirmDialog
        open={!!confirmAccept}
        onClose={() => setConfirmAccept(null)}
        onConfirm={() => confirmAccept && acceptMutation.mutate(confirmAccept)}
        isLoading={acceptMutation.isPending}
        title="Accept offer"
        message="This will create a booking. You'll need to pay to proceed."
        confirmLabel="Accept"
        variant="primary"
      />
    </div>
  );
}
