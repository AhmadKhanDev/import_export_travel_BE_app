import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { travellerTripsApi, matchesApi, offersApi } from "@/lib/services";
import { formatDate, routeLabel } from "@/lib/utils";
import { getErrorMessage } from "@/lib/api";
import { useAuth } from "@/store/AuthContext";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import type { CreateOffer } from "@/types";

export function TravellerTripDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [showOfferForm, setShowOfferForm] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState<{ matchId: string; buyerRequestId: string } | null>(null);
  const [offerForm, setOfferForm] = useState({
    itemPrice: "",
    travellerFee: "",
    platformFee: "5",
    message: "",
  });

  const { data: trip, isLoading } = useQuery({
    queryKey: ["traveller-trip", id],
    queryFn: () => travellerTripsApi.get(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const ownsTrip = user?.id === trip?.travellerId;

  const { data: matches } = useQuery({
    queryKey: ["matches-by-trip", id],
    queryFn: () => matchesApi.byTrip(id!).then((r) => r.data.data),
    enabled: !!id && ownsTrip,
  });

  const { data: offers } = useQuery({
    queryKey: ["offers-by-trip", id],
    queryFn: () => offersApi.byTrip(id!).then((r) => r.data.data),
    enabled: !!id && ownsTrip,
  });

  const publishMutation = useMutation({
    mutationFn: () => travellerTripsApi.publish(id!).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["traveller-trip", id] });
      setMessage("Trip published successfully");
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const generateMatchesMutation = useMutation({
    mutationFn: () => matchesApi.generateByTrip(id!).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["matches-by-trip", id] });
      setMessage("Matches generated");
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const sendOfferMutation = useMutation({
    mutationFn: (data: CreateOffer) => offersApi.create(data).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers-by-trip", id] });
      setMessage("Offer sent successfully");
      setShowOfferForm(false);
      setSelectedMatch(null);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const handleSendOffer = () => {
    if (!selectedMatch || !id) return;
    const payload: CreateOffer = {
      buyerRequestId: selectedMatch.buyerRequestId,
      travellerTripId: id,
      matchId: selectedMatch.matchId,
      itemPrice: Number(offerForm.itemPrice),
      travellerFee: Number(offerForm.travellerFee),
      platformFee: Number(offerForm.platformFee),
      currency: "GBP",
      message: offerForm.message || undefined,
    };
    sendOfferMutation.mutate(payload);
  };

  if (isLoading) return <Spinner />;
  if (!trip) return <Alert message="Trip not found" />;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900">
              {routeLabel(`${trip.sourceCity}, ${trip.sourceCountry}`, `${trip.destinationCity}, ${trip.destinationCountry}`)}
            </h1>
            <Badge status={trip.status} />
          </div>
          <p className="mt-1 text-slate-600">Traveller: {trip.travellerName}</p>
        </div>
        {ownsTrip && (
          <Link to="/traveller/trips">
            <Button variant="secondary" size="sm">← Back to trips</Button>
          </Link>
        )}
      </div>

      {message && <Alert message={message} type="success" />}
      {error && <Alert message={error} />}

      <Card>
        <CardBody className="grid gap-2 text-sm sm:grid-cols-2">
          <p><span className="text-slate-500">Travel date:</span> {formatDate(trip.travelDate)}</p>
          {trip.availableCapacityKg != null && (
            <p><span className="text-slate-500">Capacity:</span> {trip.availableCapacityKg} kg</p>
          )}
          {trip.allowedItemTypes && (
            <p className="sm:col-span-2"><span className="text-slate-500">Allowed items:</span> {trip.allowedItemTypes}</p>
          )}
        </CardBody>
      </Card>

      {ownsTrip && trip.status === "DRAFT" && (
        <Button onClick={() => publishMutation.mutate()} loading={publishMutation.isPending}>
          Publish trip
        </Button>
      )}

      {ownsTrip && trip.status === "PUBLISHED" && (
        <Button variant="secondary" onClick={() => generateMatchesMutation.mutate()} loading={generateMatchesMutation.isPending}>
          Find matching requests
        </Button>
      )}

      {ownsTrip && matches && matches.content.length > 0 && (
        <Card>
          <CardHeader><h2 className="font-semibold">Matches</h2></CardHeader>
          <CardBody className="divide-y divide-slate-100">
            {matches.content.map((m) => (
              <div key={m.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                <div>
                  <p className="font-medium">{m.buyerRequestTitle}</p>
                  <p className="text-sm text-slate-600">
                    {m.buyerName} · Score: {m.matchScore} · {m.itemCategory}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge status={m.status} />
                  {m.status !== "REJECTED" && (
                    <Button
                      size="sm"
                      onClick={() => {
                        setSelectedMatch({ matchId: m.id, buyerRequestId: m.buyerRequestId });
                        setShowOfferForm(true);
                      }}
                    >
                      Send offer
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </CardBody>
        </Card>
      )}

      {showOfferForm && selectedMatch && (
        <Card>
          <CardHeader><h2 className="font-semibold">Send offer</h2></CardHeader>
          <CardBody className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-3">
              <Input label="Item price (£)" type="number" step="0.01" value={offerForm.itemPrice} onChange={(e) => setOfferForm({ ...offerForm, itemPrice: e.target.value })} />
              <Input label="Your fee (£)" type="number" step="0.01" value={offerForm.travellerFee} onChange={(e) => setOfferForm({ ...offerForm, travellerFee: e.target.value })} />
              <Input label="Platform fee (£)" type="number" step="0.01" value={offerForm.platformFee} onChange={(e) => setOfferForm({ ...offerForm, platformFee: e.target.value })} />
            </div>
            <Textarea label="Message" rows={2} value={offerForm.message} onChange={(e) => setOfferForm({ ...offerForm, message: e.target.value })} />
            <div className="flex gap-3">
              <Button onClick={handleSendOffer} loading={sendOfferMutation.isPending}>Send offer</Button>
              <Button variant="secondary" onClick={() => setShowOfferForm(false)}>Cancel</Button>
            </div>
          </CardBody>
        </Card>
      )}

      {ownsTrip && offers && offers.content.length > 0 && (
        <Card>
          <CardHeader><h2 className="font-semibold">Sent offers</h2></CardHeader>
          <CardBody className="divide-y divide-slate-100">
            {offers.content.map((offer) => (
              <div key={offer.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                <div>
                  <p className="font-medium">{offer.buyerRequestTitle}</p>
                  <p className="text-sm text-slate-600">To: {offer.buyerName}</p>
                </div>
                <Badge status={offer.status} />
              </div>
            ))}
          </CardBody>
        </Card>
      )}
    </div>
  );
}
