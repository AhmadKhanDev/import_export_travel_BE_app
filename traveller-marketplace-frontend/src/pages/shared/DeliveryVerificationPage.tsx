import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { bookingApi } from "@/services/bookingApi";
import { deliveryApi } from "@/services/deliveryApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { CheckCircle, Key } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const schema = z.object({
  code: z.string().min(4, "Enter the delivery code"),
});
type FormValues = z.infer<typeof schema>;

export function DeliveryVerificationPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { user } = useAuth();
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState("");

  const { data: booking, isLoading } = useQuery({
    queryKey: ["booking", id],
    queryFn: () => bookingApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: deliveryStatus } = useQuery({
    queryKey: ["delivery-status", id],
    queryFn: () => deliveryApi.getStatus(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const generateMutation = useMutation({
    mutationFn: () => deliveryApi.generate(id!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["delivery-status", id] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const verifyMutation = useMutation({
    mutationFn: (code: string) => deliveryApi.verify(id!, code),
    onSuccess: (res) => {
      if (res.data.data.verified) {
        setVerified(true);
        qc.invalidateQueries({ queryKey: ["booking", id] });
      } else {
        setError("Incorrect code. Please try again.");
      }
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema) });

  if (isLoading) return <PageLoader />;
  if (!booking) return <ErrorMessage message="Booking not found" />;

  const isBuyer = user?.id === booking.buyerId;
  const isTraveller = user?.id === booking.travellerId;

  return (
    <div className="mx-auto max-w-md space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Delivery Verification</h1>
        <p className="text-sm text-gray-500">{booking.buyerRequestTitle}</p>
      </div>

      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-gray-800">Booking Status</h2>
          <StatusBadge status={booking.status} />
        </div>
        <div className="text-sm text-gray-600 space-y-1">
          <p><span className="font-medium">Route:</span> {booking.sourceCountry} → {booking.destinationCountry}</p>
          <p><span className="font-medium">Buyer:</span> {booking.buyerName}</p>
          <p><span className="font-medium">Traveller:</span> {booking.travellerName}</p>
        </div>
      </Card>

      {error && <ErrorMessage message={error} />}

      {verified || deliveryStatus?.verified ? (
        <Card>
          <div className="flex flex-col items-center gap-3 py-4 text-center">
            <CheckCircle size={48} className="text-emerald-500" />
            <h2 className="text-lg font-bold text-gray-900">Delivery Verified!</h2>
            <p className="text-sm text-gray-600">Payment has been released to the traveller.</p>
            <Button onClick={() => navigate(`/shared/bookings/${id}`)}>View Booking</Button>
          </div>
        </Card>
      ) : (
        <>
          {/* Buyer: generate code */}
          {isBuyer && booking.status === "PAYMENT_HELD" && (
            <Card>
              <div className="mb-3 flex items-center gap-2">
                <Key size={18} className="text-primary-600" />
                <h2 className="font-semibold text-gray-800">Generate Delivery Code</h2>
              </div>
              <p className="mb-4 text-sm text-gray-600">
                Generate a 6-digit code and share it with the traveller when they deliver your item.
              </p>
              {deliveryStatus?.codeGenerated ? (
                <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-center">
                  <p className="text-sm font-medium text-emerald-800">
                    ✓ Code generated. Share the code from your email/notification with the traveller.
                  </p>
                </div>
              ) : (
                <Button fullWidth onClick={() => generateMutation.mutate()} loading={generateMutation.isPending} icon={<Key size={15} />}>
                  Generate Code
                </Button>
              )}
            </Card>
          )}

          {/* Traveller: verify code */}
          {isTraveller && ["DELIVERED_PENDING_VERIFICATION", "PAYMENT_HELD", "IN_TRANSIT"].includes(booking.status) && (
            <Card>
              <div className="mb-3 flex items-center gap-2">
                <CheckCircle size={18} className="text-primary-600" />
                <h2 className="font-semibold text-gray-800">Enter Delivery Code</h2>
              </div>
              <p className="mb-4 text-sm text-gray-600">
                Ask the buyer for the delivery code and enter it below to confirm delivery and release payment.
              </p>
              <form onSubmit={handleSubmit((d) => { setError(""); verifyMutation.mutate(d.code); })} className="space-y-4">
                <Input
                  label="Delivery Code"
                  placeholder="e.g. 123456"
                  error={errors.code?.message}
                  {...register("code")}
                />
                <Button type="submit" fullWidth loading={isSubmitting || verifyMutation.isPending}>
                  Verify & Release Payment
                </Button>
              </form>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
