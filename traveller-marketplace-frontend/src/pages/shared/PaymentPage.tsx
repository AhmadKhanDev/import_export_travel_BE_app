import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { bookingApi } from "@/services/bookingApi";
import { paymentApi } from "@/services/paymentApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { formatMoney } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { CreditCard, CheckCircle } from "lucide-react";

export function PaymentPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [paid, setPaid] = useState(false);
  const [error, setError] = useState("");

  const { data: booking, isLoading } = useQuery({
    queryKey: ["booking", bookingId],
    queryFn: () => bookingApi.getById(bookingId!).then((r) => r.data.data),
    enabled: !!bookingId,
  });

  const payMutation = useMutation({
    mutationFn: () => paymentApi.pay(bookingId!),
    onSuccess: () => {
      setPaid(true);
      qc.invalidateQueries({ queryKey: ["booking", bookingId] });
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  if (isLoading) return <PageLoader />;
  if (!booking) return <ErrorMessage message="Booking not found" />;

  return (
    <div className="mx-auto max-w-md space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Payment</h1>
        <p className="text-sm text-gray-500">Secure escrow payment</p>
      </div>

      {paid ? (
        <Card>
          <div className="flex flex-col items-center gap-3 py-6 text-center">
            <CheckCircle size={48} className="text-emerald-500" />
            <h2 className="text-lg font-bold text-gray-900">Payment Successful!</h2>
            <p className="text-sm text-gray-600">
              Funds are held in escrow. They'll be released to the traveller after delivery is verified.
            </p>
            <Button onClick={() => navigate(`/shared/bookings/${bookingId}`)}>View Booking</Button>
          </div>
        </Card>
      ) : (
        <>
          <Card>
            <h2 className="mb-4 font-semibold text-gray-800">Order Summary</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Item</span><span>{booking.buyerRequestTitle}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Traveller</span><span>{booking.travellerName}</span></div>
              <div className="border-t border-gray-100 pt-2 space-y-1">
                <div className="flex justify-between"><span className="text-gray-500">Item Price</span><span>{formatMoney(booking.itemPrice, booking.currency)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Traveller Fee</span><span>{formatMoney(booking.travellerFee, booking.currency)}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Platform Fee</span><span>{formatMoney(booking.platformFee, booking.currency)}</span></div>
                <div className="flex justify-between border-t border-gray-200 pt-2 text-base font-bold">
                  <span>Total</span><span className="text-primary-700">{formatMoney(booking.totalAmount, booking.currency)}</span>
                </div>
              </div>
            </div>
          </Card>

          <Card>
            <div className="mb-4 flex items-center gap-2">
              <CreditCard size={18} className="text-primary-600" />
              <h2 className="font-semibold text-gray-800">Payment Method</h2>
            </div>
            <div className="rounded-xl border-2 border-primary-200 bg-primary-50 p-3">
              <p className="text-sm font-medium text-primary-800">Simulated Payment (Development)</p>
              <p className="text-xs text-primary-600">In production, this integrates with Stripe/PayPal</p>
            </div>
          </Card>

          {error && <ErrorMessage message={error} />}

          <Button fullWidth size="lg" icon={<CreditCard size={15} />} onClick={() => payMutation.mutate()} loading={payMutation.isPending}>
            Pay {formatMoney(booking.totalAmount, booking.currency)}
          </Button>
        </>
      )}
    </div>
  );
}
