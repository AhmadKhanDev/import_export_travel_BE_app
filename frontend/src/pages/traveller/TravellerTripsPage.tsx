import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { travellerTripsApi } from "@/lib/services";
import { formatDate, routeLabel } from "@/lib/utils";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import { getErrorMessage } from "@/lib/api";
import { PlaneIcon, ArrowRightIcon, MapPinIcon, CalendarIcon } from "@/components/Icons";

function PlusIcon({ size = 16 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-violet-50 text-violet-400">
        <PlaneIcon size={28} />
      </div>
      <p className="font-semibold text-slate-700">No trips yet</p>
      <p className="text-sm text-slate-500">Post your first trip to start receiving buyer requests.</p>
      <Link to="/traveller/trips/new">
        <Button size="sm"><PlusIcon size={14} /> New trip</Button>
      </Link>
    </div>
  );
}

export function TravellerTripsPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["my-traveller-trips"],
    queryFn: () => travellerTripsApi.my().then((r) => r.data.data),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="page-header mb-0">
          <h1 className="page-title">My trips</h1>
          <p className="page-subtitle">Manage your upcoming travel plans</p>
        </div>
        <Link to="/traveller/trips/new">
          <Button size="sm"><PlusIcon size={14} /> New trip</Button>
        </Link>
      </div>

      {isLoading && <Spinner />}
      {error && <Alert message={getErrorMessage(error)} />}

      {data && (
        <>
          {data.content.length === 0 ? <EmptyState /> : (
            <div className="grid gap-4">
              {data.content.map((trip) => (
                <Card key={trip.id} hover>
                  <CardBody className="flex flex-wrap items-center justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-violet-50 text-violet-600">
                        <PlaneIcon size={19} />
                      </span>
                      <div className="space-y-1.5">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="font-bold text-slate-900">
                            {routeLabel(`${trip.sourceCity}, ${trip.sourceCountry}`, `${trip.destinationCity}, ${trip.destinationCountry}`)}
                          </p>
                          <Badge status={trip.status} />
                        </div>
                        <div className="flex flex-wrap gap-3 text-xs text-slate-500">
                          <span className="flex items-center gap-1">
                            <CalendarIcon size={11} /> {formatDate(trip.travelDate)}
                          </span>
                          {trip.availableCapacityKg != null && (
                            <span className="flex items-center gap-1">
                              <MapPinIcon size={11} /> {trip.availableCapacityKg} kg capacity
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                    <Link to={`/traveller/trips/${trip.id}`}>
                      <Button variant="secondary" size="sm">
                        View <ArrowRightIcon size={13} />
                      </Button>
                    </Link>
                  </CardBody>
                </Card>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
