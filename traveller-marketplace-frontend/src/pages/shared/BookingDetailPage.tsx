import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  CreditCard,
  Truck,
  CheckCircle,
  Key,
  MessageSquare,
  LocateFixed,
  Square,
} from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { bookingApi } from "@/services/bookingApi";
import { paymentApi } from "@/services/paymentApi";
import { deliveryApi } from "@/services/deliveryApi";
import { trackingApi } from "@/services/trackingApi";
import { createRealtimeSocketClient, type RealtimeEvent } from "@/services/realtimeSocket";
import { Card, CardHeader, CardTitle } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { TrackingMapCard } from "@/components/common/TrackingMapCard";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatDate, formatDateTime, formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { getDeliveryCode, saveDeliveryCode } from "@/utils/deliveryCodeStorage";
import type { TrackingLocationUpdateRequest, TrackingSessionResponse } from "@/types/tracking";

const BUYER_GENERATE_STATUSES = ["PAYMENT_HELD", "IN_TRANSIT", "DELIVERED_PENDING_VERIFICATION"];
const TRACKING_BOOKING_STATUSES = ["PAYMENT_HELD", "IN_TRANSIT", "DELIVERED_PENDING_VERIFICATION"];

function toTrackingPayload(position: GeolocationPosition): TrackingLocationUpdateRequest {
  const heading =
    typeof position.coords.heading === "number" && Number.isFinite(position.coords.heading)
      ? position.coords.heading
      : undefined;
  const speedKph =
    typeof position.coords.speed === "number" && Number.isFinite(position.coords.speed)
      ? Number(position.coords.speed) * 3.6
      : undefined;

  return {
    latitude: position.coords.latitude,
    longitude: position.coords.longitude,
    accuracyMeters: position.coords.accuracy,
    headingDegrees: heading,
    speedKph,
  };
}

function getCurrentLocation(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) => {
    if (!("geolocation" in navigator)) {
      reject(new Error("This device does not support live location sharing."));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      resolve,
      reject,
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 15000,
      },
    );
  });
}

export function BookingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { user } = useAuth();
  const [error, setError] = useState("");
  const [trackingNotice, setTrackingNotice] = useState("");
  const [confirmPay, setConfirmPay] = useState(false);
  const [generatedCode, setGeneratedCode] = useState<string | null>(
    id ? getDeliveryCode(id) : null,
  );
  const [liveTracking, setLiveTracking] = useState<TrackingSessionResponse | null>(null);
  const lastTrackingSentAtRef = useRef(0);
  const trackingStopRequestedRef = useRef(false);

  const { data: booking, isLoading } = useQuery({
    queryKey: ["booking", id],
    queryFn: () => bookingApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: deliveryStatus } = useQuery({
    queryKey: ["delivery-status", id],
    queryFn: () => deliveryApi.getStatus(id!).then((r) => r.data.data),
    enabled: !!id && !!booking,
  });

  const { data: tracking } = useQuery({
    queryKey: ["booking-tracking", id],
    queryFn: () => trackingApi.getState(id!).then((r) => r.data.data),
    enabled: !!id && !!booking && !!user,
  });

  useEffect(() => {
    setLiveTracking(tracking ?? null);
  }, [tracking]);

  const payMutation = useMutation({
    mutationFn: () => paymentApi.pay(id!),
    onSuccess: () => { setConfirmPay(false); qc.invalidateQueries({ queryKey: ["booking", id] }); },
    onError: (err) => { setError(getErrorMessage(err)); setConfirmPay(false); },
  });

  const generateCodeMutation = useMutation({
    mutationFn: () => deliveryApi.generate(id!),
    onSuccess: (res) => {
      const code = res.data.data.code;
      setGeneratedCode(code);
      if (id) saveDeliveryCode(id, code);
      qc.invalidateQueries({ queryKey: ["delivery-status", id] });
      qc.invalidateQueries({ queryKey: ["my-notifications"] });
      qc.invalidateQueries({ queryKey: ["unread-count"] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const markInTransitMutation = useMutation({
    mutationFn: () => bookingApi.markInTransit(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["booking", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const markDeliveredMutation = useMutation({
    mutationFn: () => bookingApi.markDelivered(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["booking", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const cancelMutation = useMutation({
    mutationFn: () => bookingApi.cancel(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["booking", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const startTrackingMutation = useMutation({
    mutationFn: async () => {
      let position: GeolocationPosition;
      try {
        position = await getCurrentLocation();
      } catch (err) {
        const geoError = err as GeolocationPositionError | Error;
        if ("code" in geoError && geoError.code === geoError.PERMISSION_DENIED) {
          throw new Error("Location permission was denied. Enable it to share live tracking.");
        }
        if ("code" in geoError && geoError.code === geoError.TIMEOUT) {
          throw new Error("Timed out while trying to get your location. Please try again.");
        }
        throw new Error(
          geoError instanceof Error
            ? geoError.message
            : "Unable to read your current location.",
        );
      }

      await trackingApi.start(id!);

      try {
        return await trackingApi.updateLocation(id!, toTrackingPayload(position));
      } catch (err) {
        try {
          await trackingApi.stop(id!);
        } catch {
          // Ignore cleanup failure and surface the original location update error.
        }
        throw err;
      }
    },
    onSuccess: (res) => {
      setTrackingNotice("");
      trackingStopRequestedRef.current = false;
      setLiveTracking(res.data.data);
      qc.setQueryData(["booking-tracking", id], res.data.data);
    },
    onError: (err) => setTrackingNotice(getErrorMessage(err)),
  });

  const stopTrackingMutation = useMutation({
    mutationFn: () => trackingApi.stop(id!),
    onSuccess: (res) => {
      trackingStopRequestedRef.current = false;
      setLiveTracking(res.data.data);
      qc.setQueryData(["booking-tracking", id], res.data.data);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  useEffect(() => {
    if (!id || !user) return;

    const socket = createRealtimeSocketClient();
    const unsubscribe = socket.subscribe(
      { channel: "tracking-booking", bookingId: id },
      (event: RealtimeEvent<TrackingSessionResponse>) => {
        setLiveTracking(event.payload);
        qc.setQueryData(["booking-tracking", id], event.payload);
      },
    );

    return () => {
      unsubscribe();
    };
  }, [id, qc, user]);

  const currentTracking = liveTracking ?? tracking ?? null;

  useEffect(() => {
    if (!id || !booking || !currentTracking?.active) return;
    if (user?.id !== booking.travellerId) return;

    if (!("geolocation" in navigator)) {
      setTrackingNotice("This device does not support live location sharing.");
      if (!trackingStopRequestedRef.current) {
        trackingStopRequestedRef.current = true;
        trackingApi.stop(id).then((res) => {
          setLiveTracking(res.data.data);
          qc.setQueryData(["booking-tracking", id], res.data.data);
        });
      }
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const now = Date.now();
        if (now - lastTrackingSentAtRef.current < 5000) return;
        lastTrackingSentAtRef.current = now;
        setTrackingNotice("");
        trackingApi.updateLocation(id, toTrackingPayload(position)).catch((err) => {
          setTrackingNotice(getErrorMessage(err));
        });
      },
      (geoError) => {
        if (geoError.code === geoError.PERMISSION_DENIED) {
          setTrackingNotice("Location permission was denied. Live tracking was turned off.");
          if (!trackingStopRequestedRef.current) {
            trackingStopRequestedRef.current = true;
            trackingApi.stop(id).then((res) => {
              setLiveTracking(res.data.data);
              qc.setQueryData(["booking-tracking", id], res.data.data);
            });
          }
          return;
        }
        if (geoError.code === geoError.TIMEOUT) {
          setTrackingNotice("Timed out while trying to get your location. We'll keep trying.");
          return;
        }
        setTrackingNotice("Unable to read the traveller's current location on this device.");
      },
      {
        enableHighAccuracy: true,
        maximumAge: 5000,
        timeout: 15000,
      },
    );

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, [booking, currentTracking?.active, id, qc, user?.id]);

  if (isLoading) return <PageLoader />;
  if (!booking) return <ErrorMessage message="Booking not found" />;

  const isBuyer = user?.id === booking.buyerId;
  const isTraveller = user?.id === booking.travellerId;
  const hasActiveCode = deliveryStatus?.hasActiveCode ?? false;
  const displayCode = hasActiveCode ? generatedCode : null;
  const canShowTracking = TRACKING_BOOKING_STATUSES.includes(booking.status) || !!currentTracking?.hasLocation;

  const steps = [
    { label: "Accepted", statuses: ["ACCEPTED"] },
    { label: "Payment Pending", statuses: ["PAYMENT_PENDING"] },
    { label: "Payment Held", statuses: ["PAYMENT_HELD"] },
    { label: "In Transit", statuses: ["IN_TRANSIT"] },
    { label: "Delivered", statuses: ["DELIVERED_PENDING_VERIFICATION", "DELIVERED"] },
    { label: "Completed", statuses: ["COMPLETED"] },
  ];
  const currentStepIdx = steps.findIndex((s) => s.statuses.includes(booking.status));

  return (
    <div className="mx-auto max-w-3xl space-y-5">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">{booking.buyerRequestTitle}</h1>
          <div className="mt-1 flex items-center gap-3 text-sm text-gray-500">
            <span>{booking.sourceCountry} → {booking.destinationCountry}</span>
            <StatusBadge status={booking.status} />
          </div>
        </div>
        {["ACCEPTED", "PAYMENT_PENDING"].includes(booking.status) && (
          <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => cancelMutation.mutate()} loading={cancelMutation.isPending}>
            Cancel
          </Button>
        )}
      </div>

      {error && <ErrorMessage message={error} />}

      {/* Progress steps */}
      {!["CANCELLED", "DISPUTED"].includes(booking.status) && (
        <Card>
          <div className="flex items-center gap-0">
            {steps.map((step, i) => (
              <div key={step.label} className="flex flex-1 items-center">
                <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                  i < currentStepIdx ? "bg-emerald-500 text-white" : i === currentStepIdx ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-400"
                }`}>
                  {i < currentStepIdx ? "✓" : i + 1}
                </div>
                <div className="hidden flex-col items-start sm:flex ml-1.5">
                  <span className={`text-xs font-medium ${i <= currentStepIdx ? "text-gray-800" : "text-gray-400"}`}>
                    {step.label}
                  </span>
                </div>
                {i < steps.length - 1 && (
                  <div className={`mx-2 h-0.5 flex-1 ${i < currentStepIdx ? "bg-emerald-400" : "bg-gray-200"}`} />
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Parties */}
      <Card>
        <CardHeader><CardTitle>Parties</CardTitle></CardHeader>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-xs text-gray-500">Buyer</p>
            <p className="font-medium text-gray-800">{booking.buyerName}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Traveller</p>
            <p className="font-medium text-gray-800">{booking.travellerName}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Created</p>
            <p className="font-medium text-gray-800">{formatDate(booking.createdAt)}</p>
          </div>
          {booking.completedAt && (
            <div>
              <p className="text-xs text-gray-500">Completed</p>
              <p className="font-medium text-gray-800">{formatDate(booking.completedAt)}</p>
            </div>
          )}
        </div>
      </Card>

      {/* Financials */}
      <Card>
        <CardHeader><CardTitle>Financials</CardTitle></CardHeader>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between"><span className="text-gray-500">Item Price</span><span>{formatMoney(booking.itemPrice, booking.currency)}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Traveller Fee</span><span>{formatMoney(booking.travellerFee, booking.currency)}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Platform Fee</span><span>{formatMoney(booking.platformFee, booking.currency)}</span></div>
          <div className="flex justify-between border-t border-gray-100 pt-2 font-semibold">
            <span>Total</span><span className="text-primary-700">{formatMoney(booking.totalAmount, booking.currency)}</span>
          </div>
        </div>
      </Card>

      {/* Live tracking */}
      {canShowTracking && currentTracking && (
        <TrackingMapCard
          tracking={currentTracking}
          viewerLabel={isBuyer ? "buyer" : isTraveller ? "traveller" : "admin"}
        />
      )}

      {/* Actions */}
      <Card>
        <CardHeader><CardTitle>Actions</CardTitle></CardHeader>
        <div className="flex flex-wrap gap-3">
          {/* Buyer actions */}
          {isBuyer && booking.status === "PAYMENT_PENDING" && (
            <Button icon={<CreditCard size={15} />} onClick={() => setConfirmPay(true)}>
              Pay Now
            </Button>
          )}
          {isBuyer && BUYER_GENERATE_STATUSES.includes(booking.status) && !hasActiveCode && (
            <Button icon={<Key size={15} />} onClick={() => generateCodeMutation.mutate()} loading={generateCodeMutation.isPending}>
              Generate Delivery Code
            </Button>
          )}
          {isBuyer && BUYER_GENERATE_STATUSES.includes(booking.status) && hasActiveCode && displayCode && (
            <div className="w-full rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3">
              <p className="text-xs font-medium uppercase tracking-wide text-emerald-700">Your delivery code</p>
              <p className="mt-1 text-2xl font-bold tracking-[0.25em] text-emerald-900">{displayCode}</p>
              <p className="mt-1 text-sm text-emerald-800">Share with the traveller. Also sent to your notifications.</p>
            </div>
          )}
          {isBuyer && BUYER_GENERATE_STATUSES.includes(booking.status) && hasActiveCode && !displayCode && (
            <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3">
              <p className="text-sm font-medium text-emerald-800">
                Delivery code is active. Check your notifications for the 6-digit code.
              </p>
              <Button
                variant="secondary"
                size="sm"
                className="mt-2"
                onClick={() => generateCodeMutation.mutate()}
                loading={generateCodeMutation.isPending}
              >
                Generate New Code
              </Button>
            </div>
          )}
          {isBuyer && BUYER_GENERATE_STATUSES.includes(booking.status) && (
            <Button variant="outline" onClick={() => navigate(`/shared/delivery/${id}`)}>
              Delivery Verification
            </Button>
          )}

          {/* Traveller actions */}
          {isTraveller && booking.status === "PAYMENT_HELD" && (
            <Button icon={<Truck size={15} />} onClick={() => markInTransitMutation.mutate()} loading={markInTransitMutation.isPending}>
              Mark In Transit
            </Button>
          )}
          {isTraveller && booking.status === "IN_TRANSIT" && (
            <Button icon={<CheckCircle size={15} />} onClick={() => markDeliveredMutation.mutate()} loading={markDeliveredMutation.isPending}>
              Mark Delivered
            </Button>
          )}
          {isTraveller && booking.status === "DELIVERED_PENDING_VERIFICATION" && (
            <Button onClick={() => navigate(`/shared/delivery/${id}`)}>
              Verify Delivery Code
            </Button>
          )}
          {isTraveller && currentTracking?.shareable && !currentTracking.active && (
            <Button
              icon={<LocateFixed size={15} />}
              onClick={() => startTrackingMutation.mutate()}
              loading={startTrackingMutation.isPending}
            >
              Start Live Tracking
            </Button>
          )}
          {isTraveller && currentTracking?.shareable && currentTracking.active && (
            <Button
              variant="outline"
              icon={<Square size={15} />}
              onClick={() => stopTrackingMutation.mutate()}
              loading={stopTrackingMutation.isPending}
            >
              Stop Live Tracking
            </Button>
          )}

          {/* Chat */}
          {["PAYMENT_HELD", "IN_TRANSIT", "DELIVERED_PENDING_VERIFICATION"].includes(booking.status) && (
            <Button variant="secondary" icon={<MessageSquare size={15} />} onClick={() => navigate(`/shared/chat?bookingId=${id}`)}>
              Chat
            </Button>
          )}

          {/* Review */}
          {booking.status === "COMPLETED" && (
            <Button variant="outline" onClick={() => navigate(`/shared/reviews?bookingId=${id}`)}>
              Leave Review
            </Button>
          )}
        </div>
        {isTraveller && trackingNotice && (
          <ErrorMessage message={trackingNotice} type="warning" className="mt-4" />
        )}
        {currentTracking?.active && currentTracking.lastLocationAt && (
          <p className="mt-4 text-xs text-gray-500">
            Latest location update: {formatDateTime(currentTracking.lastLocationAt)}
          </p>
        )}
      </Card>

      <ConfirmDialog
        open={confirmPay}
        onClose={() => setConfirmPay(false)}
        onConfirm={() => payMutation.mutate()}
        isLoading={payMutation.isPending}
        title="Confirm Payment"
        message={`Pay ${formatMoney(booking.totalAmount, booking.currency)} to hold funds in escrow? The traveller will receive payment after delivery is verified.`}
        confirmLabel="Confirm Payment"
        variant="primary"
      />
    </div>
  );
}
