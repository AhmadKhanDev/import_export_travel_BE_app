import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { disputeApi } from "@/services/disputeApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { TextArea } from "@/components/ui/TextArea";
import { Input } from "@/components/ui/Input";
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
import type { DisputeReason } from "@/types/dispute";

const schema = z.object({
  bookingId: z.string().uuid("Invalid booking ID"),
  reason: z.enum(["ITEM_NOT_DELIVERED", "ITEM_DAMAGED", "WRONG_ITEM", "PAYMENT_ISSUE", "OTHER"]),
  description: z.string().min(10, "Please provide more details (min 10 chars)"),
});
type FormValues = z.infer<typeof schema>;

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

  const createMutation = useMutation({
    mutationFn: (data: FormValues) => disputeApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["my-disputes"] });
      setShowModal(false);
    },
  });

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { bookingId: bookingIdParam ?? "" },
  });

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
          <Input
            label="Booking ID"
            placeholder="Paste the booking UUID"
            error={errors.bookingId?.message}
            {...register("bookingId")}
          />
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
