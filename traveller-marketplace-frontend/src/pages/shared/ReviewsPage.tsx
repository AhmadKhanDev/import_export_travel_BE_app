import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import { reviewApi } from "@/services/reviewApi";
import { Card, CardHeader, CardTitle } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { TextArea } from "@/components/ui/TextArea";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { Modal } from "@/components/ui/Modal";
import { Star } from "lucide-react";
import { formatDate } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const reviewSchema = z.object({
  rating: z.coerce.number().min(1).max(5),
  comment: z.string().optional(),
});
type ReviewForm = z.infer<typeof reviewSchema>;

function StarPicker({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(star)}
          className={`transition-colors ${star <= value ? "text-amber-400" : "text-gray-300"}`}
        >
          <Star size={24} fill={star <= value ? "currentColor" : "none"} />
        </button>
      ))}
    </div>
  );
}

export function ReviewsPage() {
  const [searchParams] = useSearchParams();
  const bookingId = searchParams.get("bookingId");
  const { user } = useAuth();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [hoverRating, setHoverRating] = useState(0);
  const [selectedRating, setSelectedRating] = useState(0);

  const { data: receivedData, isLoading } = useQuery({
    queryKey: ["my-received-reviews", page],
    queryFn: () => reviewApi.myReceived(page, 10).then((r) => r.data.data),
  });

  const { data: givenData } = useQuery({
    queryKey: ["my-given-reviews"],
    queryFn: () => reviewApi.myGiven(0, 10).then((r) => r.data.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: ReviewForm) =>
      reviewApi.create({ bookingId: bookingId!, rating: data.rating, comment: data.comment }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["my-received-reviews"] });
      setShowCreateModal(false);
    },
  });

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<ReviewForm>({
    resolver: zodResolver(reviewSchema),
    defaultValues: { rating: 0 },
  });

  const ratingValue = watch("rating");

  if (isLoading) return <PageLoader />;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Reviews</h1>
          <p className="text-sm text-gray-500">Your ratings and feedback</p>
        </div>
        {bookingId && (
          <Button onClick={() => setShowCreateModal(true)}>Leave a Review</Button>
        )}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Received */}
        <Card>
          <CardHeader><CardTitle>Reviews Received</CardTitle></CardHeader>
          {receivedData?.content?.length === 0 ? (
            <EmptyState icon={Star} title="No reviews yet" />
          ) : (
            <div className="space-y-3">
              {receivedData?.content?.map((r) => (
                <div key={r.id} className="rounded-xl bg-gray-50 p-3">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-gray-800">{r.reviewerName}</p>
                    <div className="flex gap-0.5">
                      {[1,2,3,4,5].map((s) => (
                        <Star key={s} size={12} className={s <= r.rating ? "text-amber-400 fill-amber-400" : "text-gray-300"} />
                      ))}
                    </div>
                  </div>
                  {r.comment && <p className="mt-1 text-sm text-gray-600">{r.comment}</p>}
                  <p className="mt-1 text-xs text-gray-400">{formatDate(r.createdAt)}</p>
                </div>
              ))}
            </div>
          )}
          <div className="mt-3">
            <Pagination page={page} totalPages={receivedData?.totalPages ?? 0} totalElements={receivedData?.totalElements ?? 0} size={10} onPageChange={setPage} />
          </div>
        </Card>

        {/* Given */}
        <Card>
          <CardHeader><CardTitle>Reviews Given</CardTitle></CardHeader>
          {givenData?.content?.length === 0 ? (
            <EmptyState icon={Star} title="No reviews given" />
          ) : (
            <div className="space-y-3">
              {givenData?.content?.map((r) => (
                <div key={r.id} className="rounded-xl bg-gray-50 p-3">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-gray-800">To: {r.revieweeName}</p>
                    <div className="flex gap-0.5">
                      {[1,2,3,4,5].map((s) => (
                        <Star key={s} size={12} className={s <= r.rating ? "text-amber-400 fill-amber-400" : "text-gray-300"} />
                      ))}
                    </div>
                  </div>
                  {r.comment && <p className="mt-1 text-sm text-gray-600">{r.comment}</p>}
                  <p className="mt-1 text-xs text-gray-400">{formatDate(r.createdAt)}</p>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* Create review modal */}
      <Modal open={showCreateModal} onClose={() => setShowCreateModal(false)} title="Leave a Review" size="sm">
        <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
          <div>
            <p className="mb-2 text-sm font-medium text-gray-700">Rating</p>
            <StarPicker value={ratingValue} onChange={(v) => setValue("rating", v)} />
            {errors.rating && <p className="mt-1 text-xs text-red-600">{errors.rating.message}</p>}
          </div>
          <TextArea label="Comment (optional)" placeholder="Share your experience..." {...register("comment")} />
          {createMutation.error && <ErrorMessage message={getErrorMessage(createMutation.error)} />}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setShowCreateModal(false)}>Cancel</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Submit Review</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
