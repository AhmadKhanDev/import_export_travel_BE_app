import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Zap, MapPin, Calendar } from "lucide-react";
import { travellerTripApi } from "@/services/travellerTripApi";
import { matchingApi } from "@/services/matchingApi";
import { offerApi } from "@/services/offerApi";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { Card, CardHeader, CardTitle } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Table } from "@/components/ui/Table";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { TextArea } from "@/components/ui/TextArea";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { MatchResponse } from "@/types/matching";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const offerSchema = z.object({
  travellerFee: z.coerce.number().positive("Fee must be positive"),
  message: z.string().optional(),
});
type OfferForm = z.infer<typeof offerSchema>;

export function TravellerTripDetailPage() {
  const { id } = useParams<{ id: string }>();
  const qc = useQueryClient();
  const [error, setError] = useState("");
  const [offerModal, setOfferModal] = useState<{ requestId: string; requestTitle: string } | null>(null);

  const { data: trip, isLoading } = useQuery({
    queryKey: ["traveller-trip", id],
    queryFn: () => travellerTripApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: matchesData } = useQuery({
    queryKey: ["matches-by-trip", id],
    queryFn: () => matchingApi.getByTravellerTrip(id!).then((r) => r.data.data),
    enabled: !!id && trip?.status === "PUBLISHED",
  });

  const publishMutation = useMutation({
    mutationFn: () => travellerTripApi.publish(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["traveller-trip", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: () => travellerTripApi.cancel(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["traveller-trip", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const generateMatchMutation = useMutation({
    mutationFn: () => matchingApi.generateByTravellerTrip(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["matches-by-trip", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const {
    register: offerReg,
    handleSubmit: handleOfferSubmit,
    formState: { errors: offerErrors, isSubmitting: offerSubmitting },
    reset: resetOffer,
  } = useForm<OfferForm>({ resolver: zodResolver(offerSchema) });

  const sendOfferMutation = useMutation({
    mutationFn: (data: OfferForm) =>
      offerApi.create({
        buyerRequestId: offerModal!.requestId,
        travellerTripId: id!,
        travellerFee: data.travellerFee,
        message: data.message,
      }),
    onSuccess: () => {
      setOfferModal(null);
      resetOffer();
      qc.invalidateQueries({ queryKey: ["matches-by-trip", id] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <PageLoader />;
  if (!trip) return <ErrorMessage message="Trip not found" />;

  return (
    <div className="mx-auto max-w-3xl space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">
            {trip.sourceCity} → {trip.destinationCity}
          </h1>
          <div className="mt-1 flex items-center gap-3 text-sm text-gray-500">
            <span className="flex items-center gap-1"><Calendar size={13} /> {formatDate(trip.travelDate)}</span>
            <StatusBadge status={trip.status} />
          </div>
        </div>
        <div className="flex gap-2">
          {trip.status === "DRAFT" && (
            <Button onClick={() => publishMutation.mutate()} loading={publishMutation.isPending} size="sm">
              Publish
            </Button>
          )}
          {["PUBLISHED", "MATCHED"].includes(trip.status) && (
            <Button variant="danger" onClick={() => cancelMutation.mutate()} loading={cancelMutation.isPending} size="sm">
              Cancel
            </Button>
          )}
        </div>
      </div>

      {error && <ErrorMessage message={error} />}

      <Card>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div>
            <p className="text-xs text-gray-500">From</p>
            <p className="font-medium text-gray-800">{trip.sourceCity}, {trip.sourceCountry}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">To</p>
            <p className="font-medium text-gray-800">{trip.destinationCity}, {trip.destinationCountry}</p>
          </div>
          {trip.availableCapacityKg && (
            <div>
              <p className="text-xs text-gray-500">Capacity</p>
              <p className="font-medium text-gray-800">{trip.availableCapacityKg} kg</p>
            </div>
          )}
          {trip.returnDate && (
            <div>
              <p className="text-xs text-gray-500">Return</p>
              <p className="font-medium text-gray-800">{formatDate(trip.returnDate)}</p>
            </div>
          )}
        </div>
        {trip.notes && (
          <div className="mt-3 border-t border-gray-100 pt-3">
            <p className="text-sm text-gray-600">{trip.notes}</p>
          </div>
        )}
      </Card>

      {/* Matches */}
      {trip.status === "PUBLISHED" && (
        <Card>
          <CardHeader>
            <CardTitle>Matching Buyer Requests</CardTitle>
            <Button size="sm" icon={<Zap size={14} />} onClick={() => { setError(""); generateMatchMutation.mutate(); }} loading={generateMatchMutation.isPending}>
              Generate Matches
            </Button>
          </CardHeader>
          <Table<MatchResponse>
            columns={[
              { key: "buyerRequestTitle", header: "Request" },
              { key: "buyerName", header: "Buyer" },
              { key: "matchScore", header: "Score", render: (m) => `${Math.round(m.matchScore * 100)}%` },
              { key: "status", header: "Status", render: (m) => <StatusBadge status={m.status} /> },
              {
                key: "actions",
                header: "",
                render: (m) =>
                  ["SUGGESTED", "VIEWED"].includes(m.status) ? (
                    <Button
                      size="sm"
                      onClick={() => setOfferModal({ requestId: m.buyerRequestId, requestTitle: m.buyerRequestTitle })}
                    >
                      Send Offer
                    </Button>
                  ) : null,
              },
            ]}
            data={matchesData?.content ?? []}
            keyExtractor={(m) => m.id}
            emptyText="No matches yet. Click 'Generate Matches' to find buyer requests."
          />
        </Card>
      )}

      {/* Send offer modal */}
      <Modal open={!!offerModal} onClose={() => { setOfferModal(null); resetOffer(); }} title={`Send Offer for: ${offerModal?.requestTitle}`}>
        <form onSubmit={handleOfferSubmit((d) => sendOfferMutation.mutate(d))} className="space-y-4">
          <Input
            label="Your fee (what you want to earn)"
            type="number"
            step="0.01"
            placeholder="e.g. 30.00"
            error={offerErrors.travellerFee?.message}
            hint="The buyer's total = item price + your fee + platform fee"
            {...offerReg("travellerFee")}
          />
          <TextArea label="Message (optional)" placeholder="Tell the buyer about yourself..." {...offerReg("message")} />
          {sendOfferMutation.error && <ErrorMessage message={getErrorMessage(sendOfferMutation.error)} />}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => { setOfferModal(null); resetOffer(); }}>Cancel</Button>
            <Button type="submit" loading={offerSubmitting || sendOfferMutation.isPending}>Send Offer</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
