import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { buyerRequestsApi, travellerTripsApi } from "@/lib/services";
import { formatDate, formatMoney, routeLabel } from "@/lib/utils";
import { Badge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import { getErrorMessage } from "@/lib/api";
import {
  SearchIcon,
  ShoppingBagIcon,
  PlaneIcon,
  MapPinIcon,
  ArrowRightIcon,
  TagIcon,
  DollarIcon,
  CalendarIcon,
} from "@/components/Icons";

function EmptyState({ label }: { label: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
        <SearchIcon size={28} />
      </div>
      <p className="font-semibold text-slate-700">No {label} found</p>
      <p className="text-sm text-slate-500">Try adjusting your filters or check back later.</p>
    </div>
  );
}

export function BrowsePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = searchParams.get("tab") || "requests";
  const [sourceCountry, setSourceCountry] = useState("");
  const [destCountry, setDestCountry] = useState("");

  const params: Record<string, string> = { status: "PUBLISHED" };
  if (sourceCountry) params.sourceCountry = sourceCountry;
  if (destCountry) params.destinationCountry = destCountry;

  const requestsQuery = useQuery({
    queryKey: ["browse-requests", params],
    queryFn: () => buyerRequestsApi.list(params).then((r) => r.data.data),
    enabled: tab === "requests",
  });

  const tripsQuery = useQuery({
    queryKey: ["browse-trips", params],
    queryFn: () => travellerTripsApi.list(params).then((r) => r.data.data),
    enabled: tab === "trips",
  });

  const activeQuery = tab === "requests" ? requestsQuery : tripsQuery;

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="page-header">
        <h1 className="page-title">Browse marketplace</h1>
        <p className="page-subtitle">Find published buyer requests and traveller trips</p>
      </div>

      {/* Tab + filters */}
      <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-card">
        {/* Tabs */}
        <div className="mb-4 flex gap-2">
          {(
            [
              { key: "requests", label: "Buyer requests", icon: ShoppingBagIcon },
              { key: "trips", label: "Traveller trips", icon: PlaneIcon },
            ] as const
          ).map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              onClick={() => setSearchParams({ tab: key })}
              className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-all ${
                tab === key
                  ? "bg-brand-600 text-white shadow-brand-sm"
                  : "text-slate-600 hover:bg-slate-100"
              }`}
            >
              <Icon size={15} />
              {label}
            </button>
          ))}
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-3">
          <div className="relative">
            <MapPinIcon size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              placeholder="Source country"
              value={sourceCountry}
              onChange={(e) => setSourceCountry(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white py-2 pl-9 pr-3 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            />
          </div>
          <div className="relative">
            <MapPinIcon size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              placeholder="Destination country"
              value={destCountry}
              onChange={(e) => setDestCountry(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white py-2 pl-9 pr-3 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            />
          </div>
          {(sourceCountry || destCountry) && (
            <button
              onClick={() => { setSourceCountry(""); setDestCountry(""); }}
              className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-500 hover:bg-slate-50"
            >
              Clear
            </button>
          )}
        </div>
      </div>

      {activeQuery.isLoading && <Spinner />}
      {activeQuery.isError && <Alert message={getErrorMessage(activeQuery.error)} />}

      {/* Requests list */}
      {tab === "requests" && requestsQuery.data && (
        <>
          {requestsQuery.data.content.length === 0 ? (
            <EmptyState label="requests" />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {requestsQuery.data.content.map((req) => (
                <Card key={req.id} hover>
                  <CardBody className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex items-start gap-3">
                        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
                          <ShoppingBagIcon size={17} />
                        </span>
                        <div>
                          <p className="font-bold text-slate-900 leading-snug">{req.title}</p>
                          <p className="text-xs text-slate-500 mt-0.5">{req.buyerName}</p>
                        </div>
                      </div>
                      <Badge status={req.status} />
                    </div>

                    <div className="space-y-1.5 text-xs text-slate-600">
                      <div className="flex items-center gap-1.5">
                        <MapPinIcon size={12} className="shrink-0 text-slate-400" />
                        {routeLabel(
                          `${req.sourceCity}, ${req.sourceCountry}`,
                          `${req.destinationCity}, ${req.destinationCountry}`,
                        )}
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="flex items-center gap-1">
                          <TagIcon size={12} className="text-slate-400" />
                          {req.itemCategory}
                        </span>
                        {req.estimatedItemPrice != null && (
                          <span className="flex items-center gap-1">
                            <DollarIcon size={12} className="text-slate-400" />
                            {formatMoney(req.estimatedItemPrice)}
                          </span>
                        )}
                      </div>
                    </div>

                    <Link
                      to={`/buyer/requests/${req.id}`}
                      className="flex items-center gap-1 text-xs font-semibold text-brand-600 hover:text-brand-700"
                    >
                      View details <ArrowRightIcon size={12} />
                    </Link>
                  </CardBody>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {/* Trips list */}
      {tab === "trips" && tripsQuery.data && (
        <>
          {tripsQuery.data.content.length === 0 ? (
            <EmptyState label="trips" />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {tripsQuery.data.content.map((trip) => (
                <Card key={trip.id} hover>
                  <CardBody className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex items-start gap-3">
                        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-violet-50 text-violet-600">
                          <PlaneIcon size={17} />
                        </span>
                        <div>
                          <p className="font-bold text-slate-900 leading-snug">
                            {routeLabel(
                              `${trip.sourceCity}, ${trip.sourceCountry}`,
                              `${trip.destinationCity}, ${trip.destinationCountry}`,
                            )}
                          </p>
                          <p className="text-xs text-slate-500 mt-0.5">{trip.travellerName}</p>
                        </div>
                      </div>
                      <Badge status={trip.status} />
                    </div>

                    <div className="flex flex-wrap gap-3 text-xs text-slate-600">
                      <span className="flex items-center gap-1">
                        <CalendarIcon size={12} className="text-slate-400" />
                        {formatDate(trip.travelDate)}
                      </span>
                      {trip.availableCapacityKg != null && (
                        <span className="text-slate-500">{trip.availableCapacityKg} kg capacity</span>
                      )}
                    </div>

                    <Link
                      to={`/traveller/trips/${trip.id}`}
                      className="flex items-center gap-1 text-xs font-semibold text-brand-600 hover:text-brand-700"
                    >
                      View details <ArrowRightIcon size={12} />
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
