import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { buyerRequestsApi, matchesApi, offersApi } from "@/lib/services";
import { formatDate, formatMoney, routeLabel } from "@/lib/utils";
import { getErrorMessage } from "@/lib/api";
import { useAuth } from "@/store/AuthContext";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";

export function BuyerRequestDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const isOwner = user?.role === "BUYER";

  const { data: request, isLoading } = useQuery({
    queryKey: ["buyer-request", id],
    queryFn: () => buyerRequestsApi.get(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: matches } = useQuery({
    queryKey: ["matches-by-request", id],
    queryFn: () => matchesApi.byRequest(id!).then((r) => r.data.data),
    enabled: !!id && isOwner,
  });

  const { data: offers } = useQuery({
    queryKey: ["offers-by-request", id],
    queryFn: () => offersApi.byRequest(id!).then((r) => r.data.data),
    enabled: !!id && isOwner,
  });

  const publishMutation = useMutation({
    mutationFn: () => buyerRequestsApi.publish(id!).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["buyer-request", id] });
      setMessage("Request published successfully");
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const generateMatchesMutation = useMutation({
    mutationFn: () => matchesApi.generateByRequest(id!).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["matches-by-request", id] });
      setMessage("Matches generated");
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const acceptOfferMutation = useMutation({
    mutationFn: (offerId: string) => offersApi.accept(offerId).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["offers-by-request", id] });
      setMessage("Offer accepted — booking created");
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const rejectOfferMutation = useMutation({
    mutationFn: (offerId: string) => offersApi.reject(offerId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["offers-by-request", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <Spinner />;
  if (!request) return <Alert message="Request not found" />;

  const ownsRequest = user?.id === request.buyerId;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900">{request.title}</h1>
            <Badge status={request.status} />
          </div>
          <p className="mt-1 text-slate-600">
            {routeLabel(`${request.sourceCity}, ${request.sourceCountry}`, `${request.destinationCity}, ${request.destinationCountry}`)}
          </p>
        </div>
        {ownsRequest && (
          <Link to="/buyer/requests">
            <Button variant="secondary" size="sm">← Back to requests</Button>
          </Link>
        )}
      </div>

      {message && <Alert message={message} type="success" />}
      {error && <Alert message={error} />}

      <Card>
        <CardBody className="space-y-3">
          {request.description && <p className="text-slate-700">{request.description}</p>}
          <div className="grid gap-2 text-sm sm:grid-cols-2">
            <p><span className="text-slate-500">Category:</span> {request.itemCategory}</p>
            {request.brand && <p><span className="text-slate-500">Brand:</span> {request.brand}</p>}
            {request.estimatedItemPrice != null && (
              <p><span className="text-slate-500">Est. price:</span> {formatMoney(request.estimatedItemPrice)}</p>
            )}
            {request.neededBefore && (
              <p><span className="text-slate-500">Needed by:</span> {formatDate(request.neededBefore)}</p>
            )}
            <p><span className="text-slate-500">Buyer:</span> {request.buyerName}</p>
          </div>
        </CardBody>
      </Card>

      {ownsRequest && request.status === "DRAFT" && (
        <Button onClick={() => publishMutation.mutate()} loading={publishMutation.isPending}>
          Publish request
        </Button>
      )}

      {ownsRequest && request.status === "PUBLISHED" && (
        <Button
          variant="secondary"
          onClick={() => generateMatchesMutation.mutate()}
          loading={generateMatchesMutation.isPending}
        >
          Find matching trips
        </Button>
      )}

      {ownsRequest && matches && matches.content.length > 0 && (
        <Card>
          <CardHeader><h2 className="font-semibold">Matches</h2></CardHeader>
          <CardBody className="divide-y divide-slate-100">
            {matches.content.map((m) => (
              <div key={m.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                <div>
                  <p className="font-medium">{m.travellerName}</p>
                  <p className="text-sm text-slate-600">
                    Score: {m.matchScore} · Travel: {formatDate(m.travelDate)}
                  </p>
                </div>
                <Badge status={m.status} />
              </div>
            ))}
          </CardBody>
        </Card>
      )}

      {ownsRequest && offers && offers.content.length > 0 && (
        <Card>
          <CardHeader><h2 className="font-semibold">Offers</h2></CardHeader>
          <CardBody className="divide-y divide-slate-100">
            {offers.content.map((offer) => (
              <div key={offer.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                <div>
                  <p className="font-medium">{offer.travellerName}</p>
                  <p className="text-sm text-slate-600">
                    Total: {formatMoney(offer.totalAmount, offer.currency)} · Fee: {formatMoney(offer.travellerFee, offer.currency)}
                  </p>
                  {offer.message && <p className="text-sm text-slate-500">{offer.message}</p>}
                </div>
                <div className="flex items-center gap-2">
                  <Badge status={offer.status} />
                  {offer.status === "SENT" && (
                    <>
                      <Button size="sm" onClick={() => acceptOfferMutation.mutate(offer.id)} loading={acceptOfferMutation.isPending}>
                        Accept
                      </Button>
                      <Button size="sm" variant="danger" onClick={() => rejectOfferMutation.mutate(offer.id)}>
                        Reject
                      </Button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </CardBody>
        </Card>
      )}
    </div>
  );
}
