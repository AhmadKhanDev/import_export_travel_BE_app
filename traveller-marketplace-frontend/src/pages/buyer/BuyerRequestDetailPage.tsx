import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { MapPin, Tag, Calendar, GitMerge, FileText, Zap } from "lucide-react";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { matchingApi } from "@/services/matchingApi";
import { offerApi } from "@/services/offerApi";
import { Card, CardHeader, CardTitle } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Table } from "@/components/ui/Table";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { MatchResponse } from "@/types/matching";
import type { OfferResponse } from "@/types/offer";

export function BuyerRequestDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [error, setError] = useState("");
  const [confirmAccept, setConfirmAccept] = useState<string | null>(null);

  const { data: req, isLoading } = useQuery({
    queryKey: ["buyer-request", id],
    queryFn: () => buyerRequestApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: matchesData } = useQuery({
    queryKey: ["matches-by-request", id],
    queryFn: () => matchingApi.getByBuyerRequest(id!).then((r) => r.data.data),
    enabled: !!id && req?.status === "PUBLISHED",
  });

  const { data: offersData } = useQuery({
    queryKey: ["offers-by-request", id],
    queryFn: () => offerApi.byBuyerRequest(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const generateMatchMutation = useMutation({
    mutationFn: () => matchingApi.generateByBuyerRequest(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["matches-by-request", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const publishMutation = useMutation({
    mutationFn: () => buyerRequestApi.publish(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["buyer-request", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: () => buyerRequestApi.cancel(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["buyer-request", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const acceptOfferMutation = useMutation({
    mutationFn: (offerId: string) => offerApi.accept(offerId),
    onSuccess: (res) => {
      setConfirmAccept(null);
      navigate(`/shared/bookings/${res.data.data.id}`);
    },
    onError: (err) => { setError(getErrorMessage(err)); setConfirmAccept(null); },
  });

  const rejectOfferMutation = useMutation({
    mutationFn: (offerId: string) => offerApi.reject(offerId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["offers-by-request", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <PageLoader />;
  if (!req) return <ErrorMessage message="Request not found" />;

  return (
    <div className="mx-auto max-w-3xl space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">{req.title}</h1>
          <div className="mt-1 flex items-center gap-3 text-sm text-gray-500">
            <span className="flex items-center gap-1"><MapPin size={13} />{req.sourceCountry} → {req.destinationCountry}</span>
            <StatusBadge status={req.status} />
          </div>
        </div>
        <div className="flex gap-2">
          {req.status === "DRAFT" && (
            <Button onClick={() => publishMutation.mutate()} loading={publishMutation.isPending} size="sm">
              Publish
            </Button>
          )}
          {["PUBLISHED", "MATCHED"].includes(req.status) && (
            <Button variant="danger" onClick={() => cancelMutation.mutate()} loading={cancelMutation.isPending} size="sm">
              Cancel
            </Button>
          )}
        </div>
      </div>

      {error && <ErrorMessage message={error} />}

      {/* Details */}
      <Card>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div>
            <p className="text-xs text-gray-500">Category</p>
            <p className="font-medium text-gray-800">{req.itemCategory}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Reward</p>
            <p className="font-medium text-emerald-700">{formatMoney(req.travellersReward, req.currency)}</p>
          </div>
          {req.estimatedItemPrice && (
            <div>
              <p className="text-xs text-gray-500">Item Price</p>
              <p className="font-medium text-gray-800">{formatMoney(req.estimatedItemPrice, req.currency)}</p>
            </div>
          )}
          {req.deadlineDate && (
            <div>
              <p className="text-xs text-gray-500">Deadline</p>
              <p className="font-medium text-gray-800">{formatDate(req.deadlineDate)}</p>
            </div>
          )}
        </div>
        {req.description && (
          <div className="mt-3 border-t border-gray-100 pt-3">
            <p className="text-sm text-gray-600">{req.description}</p>
          </div>
        )}
      </Card>

      {/* Generate matches */}
      {req.status === "PUBLISHED" && (
        <Card>
          <CardHeader>
            <CardTitle>Matches</CardTitle>
            <Button
              size="sm"
              icon={<Zap size={14} />}
              onClick={() => { setError(""); generateMatchMutation.mutate(); }}
              loading={generateMatchMutation.isPending}
            >
              Generate Matches
            </Button>
          </CardHeader>
          <Table<MatchResponse>
            columns={[
              { key: "travellerName", header: "Traveller" },
              { key: "matchScore", header: "Score", render: (m) => `${Math.round(m.matchScore * 100)}%` },
              { key: "status", header: "Status", render: (m) => <StatusBadge status={m.status} /> },
            ]}
            data={matchesData?.content ?? []}
            keyExtractor={(m) => m.id}
            emptyText="No matches yet. Click 'Generate Matches' to find travellers."
          />
        </Card>
      )}

      {/* Offers */}
      <Card>
        <CardHeader>
          <CardTitle>Offers Received</CardTitle>
          <span className="text-sm text-gray-500">{offersData?.totalElements ?? 0} offers</span>
        </CardHeader>
        <Table<OfferResponse>
          columns={[
            { key: "travellerName", header: "Traveller" },
            { key: "totalAmount", header: "Total", render: (o) => formatMoney(o.totalAmount, o.currency) },
            { key: "travellerFee", header: "Traveller Fee", render: (o) => formatMoney(o.travellerFee, o.currency) },
            { key: "status", header: "Status", render: (o) => <StatusBadge status={o.status} /> },
            {
              key: "actions",
              header: "",
              render: (o) =>
                o.status === "SENT" ? (
                  <div className="flex gap-1.5">
                    <Button size="sm" onClick={() => setConfirmAccept(o.id)}>Accept</Button>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => rejectOfferMutation.mutate(o.id)}
                      loading={rejectOfferMutation.isPending}
                    >
                      Reject
                    </Button>
                  </div>
                ) : null,
            },
          ]}
          data={offersData?.content ?? []}
          keyExtractor={(o) => o.id}
          emptyText="No offers received yet."
        />
      </Card>

      <ConfirmDialog
        open={!!confirmAccept}
        onClose={() => setConfirmAccept(null)}
        onConfirm={() => confirmAccept && acceptOfferMutation.mutate(confirmAccept)}
        isLoading={acceptOfferMutation.isPending}
        title="Accept offer"
        message="Accepting this offer will create a booking. You'll need to pay to hold the funds in escrow."
        confirmLabel="Accept & Create Booking"
        variant="primary"
      />
    </div>
  );
}
