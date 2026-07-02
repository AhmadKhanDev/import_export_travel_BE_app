import { useQuery } from "@tanstack/react-query";
import { travellerTripApi } from "@/services/travellerTripApi";
import { matchingApi } from "@/services/matchingApi";
import { Card } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { GitMerge } from "lucide-react";
import { useNavigate } from "react-router-dom";

export function TravellerMatchesPage() {
  const { data: tripsData, isLoading } = useQuery({
    queryKey: ["my-trips-published"],
    queryFn: () => travellerTripApi.my({ status: "PUBLISHED", size: 50 }).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const trips = tripsData?.content ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Matches</h1>
        <p className="text-sm text-gray-500">Buyer requests matching your published trips</p>
      </div>

      {trips.length === 0 ? (
        <EmptyState icon={GitMerge} title="No published trips" description="Publish a trip to see matching buyer requests." />
      ) : (
        trips.map((trip) => (
          <TripMatchCard key={trip.id} tripId={trip.id} tripLabel={`${trip.sourceCity} → ${trip.destinationCity}`} status={trip.status} />
        ))
      )}
    </div>
  );
}

function TripMatchCard({ tripId, tripLabel, status }: { tripId: string; tripLabel: string; status: string }) {
  const navigate = useNavigate();
  const { data: matchesData, isLoading } = useQuery({
    queryKey: ["matches-by-trip", tripId],
    queryFn: () => matchingApi.getByTravellerTrip(tripId).then((r) => r.data.data),
  });

  return (
    <Card>
      <div className="mb-3 flex items-center justify-between">
        <h2 className="font-medium text-gray-900">{tripLabel}</h2>
        <StatusBadge status={status} />
      </div>
      {isLoading ? (
        <p className="text-sm text-gray-400">Loading…</p>
      ) : matchesData?.content?.length === 0 ? (
        <p className="text-sm text-gray-400">No matches. Go to trip detail to generate.</p>
      ) : (
        <div className="space-y-2">
          {matchesData?.content?.map((m) => (
            <div
              key={m.id}
              className="flex cursor-pointer items-center justify-between rounded-lg bg-gray-50 px-3 py-2 hover:bg-gray-100"
              onClick={() => navigate(`/traveller/trips/${tripId}`)}
            >
              <div>
                <p className="text-sm font-medium text-gray-800">{m.buyerRequestTitle}</p>
                <p className="text-xs text-gray-500">Buyer: {m.buyerName} · Score: {Math.round(m.matchScore * 100)}%</p>
              </div>
              <StatusBadge status={m.status} size="sm" />
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
