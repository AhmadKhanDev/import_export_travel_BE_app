import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { bookingsApi, paymentsApi, deliveryApi, reviewsApi } from "@/lib/services";
import { formatDateTime, formatMoney, routeLabel } from "@/lib/utils";
import { getErrorMessage } from "@/lib/api";
import { useAuth } from "@/store/AuthContext";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import {
  CreditCardIcon,
  KeyIcon,
  CheckCircleIcon,
  TruckIcon,
  PackageIcon,
  MapPinIcon,
  StarIcon,
  DollarIcon,
  UserIcon,
} from "@/components/Icons";
import type { BookingStatus } from "@/types";

const workflowSteps: { status: BookingStatus; label: string; desc: string }[] = [
  { status: "ACCEPTED",       label: "Accepted",      desc: "Offer accepted, booking created" },
  { status: "PAYMENT_PENDING", label: "Pay",           desc: "Buyer makes escrow payment" },
  { status: "PAYMENT_HELD",   label: "Held",           desc: "Funds secured in escrow" },
  { status: "IN_TRANSIT",     label: "In Transit",     desc: "Traveller has the item" },
  { status: "DELIVERED_PENDING_VERIFICATION", label: "Verify", desc: "Awaiting delivery code" },
  { status: "COMPLETED",      label: "Done",           desc: "Delivered & payment released" },
];

const statusOrder: BookingStatus[] = [
  "ACCEPTED", "PAYMENT_PENDING", "PAYMENT_HELD", "IN_TRANSIT",
  "DELIVERED_PENDING_VERIFICATION", "COMPLETED",
];

function WorkflowTimeline({ current }: { current: BookingStatus }) {
  const currentIdx = statusOrder.indexOf(current);
  if (currentIdx === -1) return null;

  return (
    <div className="overflow-x-auto">
      <div className="flex min-w-max items-start gap-0">
        {workflowSteps.map((step, i) => {
          const stepIdx = statusOrder.indexOf(step.status);
          const done = stepIdx < currentIdx;
          const active = stepIdx === currentIdx;

          return (
            <div key={step.status} className="flex items-center">
              <div className="flex flex-col items-center gap-1.5 px-3 text-center">
                <div
                  className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition-colors ${
                    done
                      ? "bg-emerald-500 text-white"
                      : active
                      ? "bg-brand-600 text-white ring-4 ring-brand-100"
                      : "bg-slate-100 text-slate-400"
                  }`}
                >
                  {done ? <CheckCircleIcon size={14} /> : i + 1}
                </div>
                <p className={`text-xs font-semibold ${active ? "text-brand-700" : done ? "text-emerald-700" : "text-slate-400"}`}>
                  {step.label}
                </p>
              </div>
              {i < workflowSteps.length - 1 && (
                <div className={`h-0.5 w-8 ${stepIdx < currentIdx ? "bg-emerald-400" : "bg-slate-200"}`} />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function BookingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [deliveryCode, setDeliveryCode] = useState("");
  const [verifyCode, setVerifyCode] = useState("");
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState("");

  const { data: booking, isLoading } = useQuery({
    queryKey: ["booking", id],
    queryFn: () => bookingsApi.get(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: payment } = useQuery({
    queryKey: ["payment", id],
    queryFn: () => paymentsApi.getByBooking(id!).then((r) => r.data.data),
    enabled: !!id && !!booking && booking.status !== "PAYMENT_PENDING",
    retry: false,
  });

  const { data: reviews } = useQuery({
    queryKey: ["reviews-booking", id],
    queryFn: () => reviewsApi.byBooking(id!).then((r) => r.data.data),
    enabled: !!id && booking?.status === "COMPLETED",
    retry: false,
  });

  const isBuyer = user?.id === booking?.buyerId;
  const isTraveller = user?.id === booking?.travellerId;

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["booking", id] });
    queryClient.invalidateQueries({ queryKey: ["payment", id] });
  };

  const payMutation = useMutation({
    mutationFn: () => paymentsApi.pay(id!).then((r) => r.data.data),
    onSuccess: () => { setMessage("Payment successful — funds held in escrow"); invalidate(); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const generateCodeMutation = useMutation({
    mutationFn: () => deliveryApi.generate(id!).then((r) => r.data.data),
    onSuccess: (data) => { setDeliveryCode(data.code); setMessage("Code generated — share in person with the traveller"); invalidate(); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const verifyMutation = useMutation({
    mutationFn: () => deliveryApi.verify(id!, verifyCode),
    onSuccess: () => { setMessage("Delivery verified — payment released to traveller"); invalidate(); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const inTransitMutation = useMutation({
    mutationFn: () => bookingsApi.markInTransit(id!).then((r) => r.data.data),
    onSuccess: () => { setMessage("Marked as in transit"); invalidate(); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const deliveredMutation = useMutation({
    mutationFn: () => bookingsApi.markDelivered(id!).then((r) => r.data.data),
    onSuccess: () => { setMessage("Marked as delivered — awaiting buyer verification"); invalidate(); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const reviewMutation = useMutation({
    mutationFn: () => reviewsApi.create({ bookingId: id!, rating: reviewRating, comment: reviewComment || undefined }),
    onSuccess: () => { setMessage("Review submitted"); queryClient.invalidateQueries({ queryKey: ["reviews-booking", id] }); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <Spinner />;
  if (!booking) return <Alert message="Booking not found" />;

  const hasReviewed = reviews?.some((r) => r.reviewerId === user?.id);
  const isTerminal = booking.status === "CANCELLED" || booking.status === "DISPUTED";

  return (
    <div className="space-y-6 animate-slide-up">
      {/* Header */}
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="text-2xl font-extrabold text-slate-900">{booking.buyerRequestTitle}</h1>
            <Badge status={booking.status} />
          </div>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-slate-500">
            <MapPinIcon size={13} />
            {routeLabel(
              `${booking.sourceCity}, ${booking.sourceCountry}`,
              `${booking.destinationCity}, ${booking.destinationCountry}`,
            )}
          </p>
        </div>
        <Link to="/bookings">
          <Button variant="secondary" size="sm">← Back to bookings</Button>
        </Link>
      </div>

      {message && <Alert message={message} type="success" />}
      {error && <Alert message={error} />}

      {/* Workflow timeline */}
      {!isTerminal && (
        <Card>
          <CardHeader>
            <p className="section-title">Booking progress</p>
          </CardHeader>
          <CardBody>
            <WorkflowTimeline current={booking.status} />
          </CardBody>
        </Card>
      )}

      {/* Booking details */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Parties */}
        <Card>
          <CardHeader><p className="section-title">Parties</p></CardHeader>
          <CardBody className="space-y-3">
            {[
              { label: "Buyer", value: booking.buyerName },
              { label: "Traveller", value: booking.travellerName },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center gap-3">
                <span className="flex h-8 w-8 items-center justify-center rounded-xl bg-slate-100 text-slate-500">
                  <UserIcon size={15} />
                </span>
                <div>
                  <p className="text-xs text-slate-400">{label}</p>
                  <p className="text-sm font-semibold text-slate-900">{value}</p>
                </div>
              </div>
            ))}
            {booking.acceptedAt && (
              <p className="text-xs text-slate-400 pt-1">Accepted: {formatDateTime(booking.acceptedAt)}</p>
            )}
          </CardBody>
        </Card>

        {/* Financials */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <p className="section-title">Financials</p>
              {payment && <Badge status={payment.status} />}
            </div>
          </CardHeader>
          <CardBody className="space-y-2">
            {[
              { label: "Item price", value: formatMoney(booking.itemPrice, booking.currency) },
              { label: "Traveller fee", value: formatMoney(booking.travellerFee, booking.currency) },
              { label: "Platform fee", value: formatMoney(booking.platformFee, booking.currency) },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center justify-between text-sm">
                <span className="text-slate-500">{label}</span>
                <span className="font-medium text-slate-800">{value}</span>
              </div>
            ))}
            <div className="mt-2 flex items-center justify-between border-t border-slate-100 pt-2">
              <span className="flex items-center gap-1 text-sm font-bold text-slate-900">
                <DollarIcon size={14} /> Total
              </span>
              <span className="text-base font-extrabold text-brand-700">
                {formatMoney(booking.totalAmount, booking.currency)}
              </span>
            </div>
          </CardBody>
        </Card>
      </div>

      {/* Action zone */}
      <div className="space-y-4">
        {/* Buyer: Pay */}
        {isBuyer && booking.status === "PAYMENT_PENDING" && (
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <CreditCardIcon size={18} className="text-brand-600" />
                <p className="section-title">Pay for this booking</p>
              </div>
            </CardHeader>
            <CardBody className="space-y-4">
              <p className="text-sm text-slate-600">
                Your payment of{" "}
                <strong>{formatMoney(booking.totalAmount, booking.currency)}</strong> will be
                held in escrow until delivery is confirmed.
              </p>
              <Button onClick={() => payMutation.mutate()} loading={payMutation.isPending} size="lg">
                <CreditCardIcon size={16} /> Pay now — escrow
              </Button>
            </CardBody>
          </Card>
        )}

        {/* Buyer: Generate delivery code */}
        {isBuyer &&
          (booking.status === "DELIVERED_PENDING_VERIFICATION" || booking.status === "IN_TRANSIT") &&
          !deliveryCode && (
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <KeyIcon size={18} className="text-amber-600" />
                  <p className="section-title">Generate delivery code</p>
                </div>
              </CardHeader>
              <CardBody className="space-y-4">
                <p className="text-sm text-slate-600">
                  Generate a 6-digit code and hand it to the traveller in person when you receive
                  your item.
                </p>
                <Button
                  onClick={() => generateCodeMutation.mutate()}
                  loading={generateCodeMutation.isPending}
                  variant="secondary"
                >
                  <KeyIcon size={16} /> Generate code
                </Button>
              </CardBody>
            </Card>
          )}

        {/* Delivery code display */}
        {deliveryCode && (
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <KeyIcon size={18} className="text-amber-600" />
                <p className="section-title">Your delivery code</p>
              </div>
            </CardHeader>
            <CardBody className="space-y-3">
              <div className="flex justify-center">
                <span className="rounded-2xl bg-brand-50 px-8 py-5 text-5xl font-extrabold tracking-[0.25em] text-brand-700 border-2 border-brand-200">
                  {deliveryCode}
                </span>
              </div>
              <p className="text-center text-sm text-slate-500">
                Show this code to the traveller only when you have received your item.
              </p>
            </CardBody>
          </Card>
        )}

        {/* Traveller: Mark in transit */}
        {isTraveller && booking.status === "PAYMENT_HELD" && (
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <TruckIcon size={18} className="text-violet-600" />
                <p className="section-title">Item collected</p>
              </div>
            </CardHeader>
            <CardBody className="space-y-4">
              <p className="text-sm text-slate-600">
                Confirm you have the item and are now in transit.
              </p>
              <Button onClick={() => inTransitMutation.mutate()} loading={inTransitMutation.isPending}>
                <TruckIcon size={16} /> Mark as in transit
              </Button>
            </CardBody>
          </Card>
        )}

        {/* Traveller: Mark delivered */}
        {isTraveller && booking.status === "IN_TRANSIT" && (
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <PackageIcon size={18} className="text-emerald-600" />
                <p className="section-title">Delivery confirmation</p>
              </div>
            </CardHeader>
            <CardBody className="space-y-4">
              <p className="text-sm text-slate-600">
                Mark the item as delivered. The buyer will then generate a verification code for
                you.
              </p>
              <Button onClick={() => deliveredMutation.mutate()} loading={deliveredMutation.isPending}>
                <PackageIcon size={16} /> Mark as delivered
              </Button>
            </CardBody>
          </Card>
        )}

        {/* Traveller: Enter verification code */}
        {isTraveller && booking.status === "DELIVERED_PENDING_VERIFICATION" && (
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <CheckCircleIcon size={18} className="text-emerald-600" />
                <p className="section-title">Verify delivery</p>
              </div>
            </CardHeader>
            <CardBody className="space-y-4">
              <p className="text-sm text-slate-600">
                Ask the buyer for their 6-digit verification code and enter it below. This will
                release your payment.
              </p>
              <Input
                label="Buyer's 6-digit code"
                value={verifyCode}
                onChange={(e) => setVerifyCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                maxLength={6}
                placeholder="000000"
                className="text-center text-xl tracking-widest font-bold"
                icon={<KeyIcon size={15} />}
              />
              <Button
                onClick={() => verifyMutation.mutate()}
                loading={verifyMutation.isPending}
                disabled={verifyCode.length !== 6}
              >
                <CheckCircleIcon size={16} /> Verify & release payment
              </Button>
            </CardBody>
          </Card>
        )}
      </div>

      {/* Review section */}
      {booking.status === "COMPLETED" && !hasReviewed && (isBuyer || isTraveller) && (
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <StarIcon size={18} className="text-amber-500" />
              <p className="section-title">Leave a review</p>
            </div>
          </CardHeader>
          <CardBody className="space-y-4">
            <div className="space-y-1.5">
              <p className="text-sm font-medium text-slate-700">Rating</p>
              <div className="flex gap-2">
                {[1, 2, 3, 4, 5].map((n) => (
                  <button
                    key={n}
                    type="button"
                    onClick={() => setReviewRating(n)}
                    className={`text-2xl transition-transform hover:scale-110 ${
                      n <= reviewRating ? "text-amber-400" : "text-slate-300"
                    }`}
                  >
                    ★
                  </button>
                ))}
                <span className="ml-2 self-center text-sm font-semibold text-slate-700">
                  {reviewRating} / 5
                </span>
              </div>
            </div>
            <Textarea
              label="Comment (optional)"
              rows={3}
              placeholder="Describe your experience…"
              value={reviewComment}
              onChange={(e) => setReviewComment(e.target.value)}
            />
            <Button onClick={() => reviewMutation.mutate()} loading={reviewMutation.isPending}>
              <StarIcon size={16} /> Submit review
            </Button>
          </CardBody>
        </Card>
      )}

      {/* Existing reviews */}
      {reviews && reviews.length > 0 && (
        <Card>
          <CardHeader><p className="section-title">Reviews</p></CardHeader>
          <CardBody className="divide-y divide-slate-100">
            {reviews.map((r) => (
              <div key={r.id} className="flex gap-3 py-4 first:pt-0 last:pb-0">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-xs font-bold text-brand-700">
                  {r.reviewerName[0]}
                </div>
                <div>
                  <p className="text-sm font-semibold text-slate-900">
                    {r.reviewerName}{" "}
                    <span className="text-slate-400 font-normal">→ {r.revieweeName}</span>
                  </p>
                  <p className="mt-0.5 text-sm text-amber-500">
                    {"★".repeat(r.rating)}
                    <span className="text-slate-300">{"★".repeat(5 - r.rating)}</span>
                  </p>
                  {r.comment && (
                    <p className="mt-1 text-sm text-slate-600">{r.comment}</p>
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
