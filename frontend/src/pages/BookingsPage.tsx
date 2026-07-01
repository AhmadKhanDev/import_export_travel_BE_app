import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { bookingsApi } from "@/lib/services";
import { formatMoney, routeLabel } from "@/lib/utils";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import { getErrorMessage } from "@/lib/api";
import { PackageIcon, MapPinIcon, ArrowRightIcon, DollarIcon } from "@/components/Icons";

function EmptyBookings() {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
        <PackageIcon size={28} />
      </div>
      <p className="font-semibold text-slate-700">No bookings yet</p>
      <p className="text-sm text-slate-500">
        Bookings are created when a buyer accepts a traveller's offer.
      </p>
      <Link to="/browse">
        <Button variant="secondary" size="sm">Browse marketplace</Button>
      </Link>
    </div>
  );
}

export function BookingsPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["my-bookings"],
    queryFn: () => bookingsApi.my().then((r) => r.data.data),
  });

  return (
    <div className="space-y-6">
      <div className="page-header">
        <h1 className="page-title">My bookings</h1>
        <p className="page-subtitle">Track your orders and deliveries</p>
      </div>

      {isLoading && <Spinner />}
      {error && <Alert message={getErrorMessage(error)} />}

      {data && (
        <>
          {data.content.length === 0 ? (
            <EmptyBookings />
          ) : (
            <div className="grid gap-4">
              {data.content.map((booking) => (
                <Card key={booking.id} hover>
                  <CardBody className="flex flex-wrap items-center justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                        <PackageIcon size={19} />
                      </span>
                      <div className="space-y-1.5">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="font-bold text-slate-900">{booking.buyerRequestTitle}</p>
                          <Badge status={booking.status} />
                        </div>
                        <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
                          <span className="flex items-center gap-1">
                            <MapPinIcon size={11} />
                            {routeLabel(
                              `${booking.sourceCity}, ${booking.sourceCountry}`,
                              `${booking.destinationCity}, ${booking.destinationCountry}`,
                            )}
                          </span>
                          <span className="flex items-center gap-1">
                            <DollarIcon size={11} />
                            {formatMoney(booking.totalAmount, booking.currency)}
                          </span>
                        </div>
                        <p className="text-xs text-slate-400">
                          {booking.buyerName} ↔ {booking.travellerName}
                        </p>
                      </div>
                    </div>
                    <Link to={`/bookings/${booking.id}`}>
                      <Button variant="secondary" size="sm">
                        Manage <ArrowRightIcon size={13} />
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
