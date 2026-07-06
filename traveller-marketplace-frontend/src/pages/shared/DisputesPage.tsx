import { useMemo, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { bookingApi } from "@/services/bookingApi";
import { disputeApi } from "@/services/disputeApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { TextArea } from "@/components/ui/TextArea";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { Modal } from "@/components/ui/Modal";
import { AlertTriangle } from "lucide-react";
import { formatDate } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { DISPUTE_REASONS } from "@/utils/constants";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import type { BookingResponse } from "@/types/booking";

const schema = z.object({
  bookingId: z.string().uuid("Invalid booking ID"),
  reason: z.enum(["ITEM_NOT_DELIVERED", "ITEM_DAMAGED", "WRONG_ITEM", "PAYMENT_ISSUE", "OTHER"]),
  description: z.string().min(10, "Please provide more details (min 10 chars)"),
});
type FormValues = z.infer<typeof schema>;

const DISPUTE_ELIGIBLE_BOOKING_STATUSES = [
  "PAYMENT_HELD",
  "IN_TRANSIT",
  "DELIVERED_PENDING_VERIFICATION",
  "DELIVERED",
  "COMPLETED",
] as const;

function isDisputeEligibleBooking(booking: BookingResponse): boolean {
  return DISPUTE_ELIGIBLE_BOOKING_STATUSES.some((status) => status === booking.status);
}

export function DisputesPage() {
  const [searchParams] = useSearchParams();
  const bookingIdParam = searchParams.get("bookingId");
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [showModal, setShowModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["my-disputes", page],
    queryFn: () => disputeApi.my(undefined, page, 20).then((r) => r.data.data),
  });

  const { data: bookingsData, isLoading: bookingsLoading } = useQuery({
    queryKey: ["my-bookings-for-disputes"],
    queryFn: () => bookingApi.my(undefined, 0, 100).then((r) => r.data.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: FormValues) => disputeApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["my-disputes"] });
      setShowModal(false);
    },
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { bookingId: bookingIdParam ?? "" },
  });

  const disputeEligibleBookings = useMemo(
    () =>
      (bookingsData?.content ?? []).filter(isDisputeEligibleBooking),
    [bookingsData],
  );

  const selectedBookingId = watch("bookingId");
  const selectedBooking = disputeEligibleBookings.find((booking) => booking.id === selectedBookingId);
  const bookingOptions = disputeEligibleBookings.map((booking) => ({
    value: booking.id,
    label: `${booking.buyerRequestTitle} • ${booking.buyerName} / ${booking.travellerName}`,
  }));

  if (isLoading) return <PageLoader />;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Disputes</h1>
          <p className="text-sm text-gray-500">Raise and track disputes for bookings</p>
        </div>
        <Button icon={<AlertTriangle size={15} />} variant="danger" onClick={() => setShowModal(true)}>
          Raise Dispute
        </Button>
      </div>

      {data?.content?.length === 0 ? (
        <EmptyState icon={AlertTriangle} title="No disputes" description="You haven't raised any disputes." />
      ) : (
        <div className="space-y-3">
          {data?.content?.map((d) => (
            <Card key={d.id}>
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-medium text-gray-900">{d.reason.replace(/_/g, " ")}</p>
                  <p className="mt-1 text-sm text-gray-600">{d.description}</p>
                  {d.resolution && (
                    <div className="mt-2 rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
                      <strong>Resolution:</strong> {d.resolution}
                    </div>
                  )}
                  <p className="mt-2 text-xs text-gray-400">Filed: {formatDate(d.createdAt)}</p>
                </div>
                <StatusBadge status={d.status} />
              </div>
            </Card>
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      {/* Create dispute modal */}
      <Modal open={showModal} onClose={() => setShowModal(false)} title="Raise a Dispute">
        <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
          <Select
            label="Booking"
            options={bookingOptions}
            placeholder={
              bookingsLoading
                ? "Loading eligible bookings..."
                : bookingOptions.length > 0
                  ? "Select a booking"
                  : "No active dispute-eligible bookings found"
            }
            error={errors.bookingId?.message}
            disabled={bookingsLoading || bookingOptions.length === 0}
            {...register("bookingId")}
          />
          {selectedBooking && (
            <div className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-3">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-medium text-gray-900">{selectedBooking.buyerRequestTitle}</p>
                  <p className="mt-1 text-sm text-gray-600">
                    {selectedBooking.sourceCountry} to {selectedBooking.destinationCountry}
                  </p>
                  <p className="mt-1 text-xs text-gray-500">
                    Buyer: {selectedBooking.buyerName} | Traveller: {selectedBooking.travellerName}
                  </p>
                  <p className="mt-2 text-xs font-mono text-gray-500">
                    Booking ID: {selectedBooking.id}
                  </p>
                </div>
                <StatusBadge status={selectedBooking.status} size="sm" />
              </div>
            </div>
          )}
          {!selectedBooking && disputeEligibleBookings.length > 0 && (
            <div className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-800">
              Select a booking to see its full ID and current status.
            </div>
          )}
          {disputeEligibleBookings.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-gray-700">Bookings currently active or recently completed</p>
              <div className="max-h-52 space-y-2 overflow-y-auto rounded-xl border border-gray-200 bg-white p-2">
                {disputeEligibleBookings.map((booking: BookingResponse) => (
                  <button
                    key={booking.id}
                    type="button"
                    className={`w-full rounded-lg border px-3 py-3 text-left transition-colors ${
                      selectedBookingId === booking.id
                        ? "border-primary-600 bg-primary-50"
                        : "border-gray-200 hover:bg-gray-50"
                    }`}
                    onClick={() => setValue("bookingId", booking.id, { shouldValidate: true, shouldDirty: true })}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="font-medium text-gray-900">{booking.buyerRequestTitle}</p>
                        <p className="mt-1 text-sm text-gray-600">
                          {booking.sourceCountry} to {booking.destinationCountry}
                        </p>
                        <p className="mt-1 text-xs text-gray-500">
                          Buyer: {booking.buyerName} | Traveller: {booking.travellerName}
                        </p>
                        <p className="mt-1 text-xs font-mono text-gray-400">{booking.id}</p>
                      </div>
                      <StatusBadge status={booking.status} size="sm" />
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
          <Select
            label="Reason"
            options={DISPUTE_REASONS.map((r) => ({ value: r.value, label: r.label }))}
            placeholder="Select reason"
            error={errors.reason?.message}
            {...register("reason")}
          />
          <TextArea
            label="Description"
            placeholder="Describe the issue in detail..."
            error={errors.description?.message}
            {...register("description")}
          />
          {createMutation.error && <ErrorMessage message={getErrorMessage(createMutation.error)} />}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
            <Button type="submit" variant="danger" loading={isSubmitting || createMutation.isPending}>Submit Dispute</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
