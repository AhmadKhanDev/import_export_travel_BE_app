import { MapPin, Navigation, Radio, ShieldAlert } from "lucide-react";
import { Card, CardHeader, CardTitle } from "@/components/ui/Card";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import type { TrackingSessionResponse } from "@/types/tracking";
import { formatDateTime } from "@/utils/formatters";

interface TrackingMapCardProps {
  tracking: TrackingSessionResponse;
  viewerLabel: "buyer" | "traveller" | "admin";
}

function buildMapUrl(latitude: number, longitude: number) {
  const delta = 0.02;
  const left = longitude - delta;
  const right = longitude + delta;
  const top = latitude + delta;
  const bottom = latitude - delta;

  return `https://www.openstreetmap.org/export/embed.html?bbox=${left}%2C${bottom}%2C${right}%2C${top}&layer=mapnik&marker=${latitude}%2C${longitude}`;
}

export function TrackingMapCard({ tracking, viewerLabel }: TrackingMapCardProps) {
  return (
    <Card padding="none" className="overflow-hidden">
      <div className="p-5">
        <CardHeader className="mb-3">
          <CardTitle>Live Tracking</CardTitle>
          <div className="flex items-center gap-2">
            {tracking.active ? (
              <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700">
                <Radio size={12} />
                Live
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-600">
                Offline
              </span>
            )}
          </div>
        </CardHeader>

        {!tracking.shareable && (
          <ErrorMessage
            type="info"
            message="Live tracking is available only while the booking is actively being delivered."
            className="mb-3"
          />
        )}

        {tracking.shareable && !tracking.active && (
          <ErrorMessage
            type="warning"
            message={`The traveller is not currently sharing location with the ${viewerLabel}.`}
            className="mb-3"
          />
        )}

        {tracking.active && !tracking.hasLocation && (
          <ErrorMessage
            type="info"
            message="Tracking is on, but the app is still waiting for the traveller's first location update."
            className="mb-3"
          />
        )}

        {tracking.hasLocation && tracking.latitude != null && tracking.longitude != null ? (
          <>
            <iframe
              title="Traveller live location"
              src={buildMapUrl(tracking.latitude, tracking.longitude)}
              className="h-72 w-full rounded-xl border border-gray-200"
              loading="lazy"
            />
            <div className="grid grid-cols-1 gap-3 p-5 pt-4 text-sm sm:grid-cols-3">
              <div className="rounded-xl bg-gray-50 p-3">
                <p className="mb-1 flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-gray-500">
                  <MapPin size={12} />
                  Coordinates
                </p>
                <p className="font-medium text-gray-800">
                  {tracking.latitude.toFixed(5)}, {tracking.longitude.toFixed(5)}
                </p>
              </div>
              <div className="rounded-xl bg-gray-50 p-3">
                <p className="mb-1 flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-gray-500">
                  <Navigation size={12} />
                  Accuracy
                </p>
                <p className="font-medium text-gray-800">
                  {tracking.accuracyMeters != null
                    ? `${Math.round(tracking.accuracyMeters)} m`
                    : "Unknown"}
                </p>
              </div>
              <div className="rounded-xl bg-gray-50 p-3">
                <p className="mb-1 flex items-center gap-1 text-xs font-medium uppercase tracking-wide text-gray-500">
                  <Radio size={12} />
                  Last Update
                </p>
                <p className="font-medium text-gray-800">
                  {formatDateTime(tracking.lastLocationAt)}
                </p>
              </div>
            </div>
          </>
        ) : (
          <div className="flex h-56 flex-col items-center justify-center gap-3 bg-gradient-to-br from-gray-50 to-gray-100 px-6 text-center">
            <ShieldAlert size={28} className="text-gray-400" />
            <div>
              <p className="font-medium text-gray-700">No live position yet</p>
              <p className="mt-1 text-sm text-gray-500">
                Once the traveller shares location, the map will appear here automatically.
              </p>
            </div>
          </div>
        )}
      </div>
    </Card>
  );
}
