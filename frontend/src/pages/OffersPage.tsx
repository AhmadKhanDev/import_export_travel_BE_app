import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { offersApi } from "@/lib/services";
import { formatMoney } from "@/lib/utils";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { Alert } from "@/components/ui/Alert";
import { getErrorMessage } from "@/lib/api";
import { BookOpenIcon, ArrowRightIcon, DollarIcon } from "@/components/Icons";

function EmptyOffers() {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
        <BookOpenIcon size={28} />
      </div>
      <p className="font-semibold text-slate-700">No offers yet</p>
      <p className="text-sm text-slate-500">
        Travellers send offers; buyers accept them to create a booking.
      </p>
    </div>
  );
}

export function OffersPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["my-offers"],
    queryFn: () => offersApi.my().then((r) => r.data.data),
  });

  return (
    <div className="space-y-6">
      <div className="page-header">
        <h1 className="page-title">My offers</h1>
        <p className="page-subtitle">Offers you've sent or received</p>
      </div>

      {isLoading && <Spinner />}
      {error && <Alert message={getErrorMessage(error)} />}

      {data && (
        <>
          {data.content.length === 0 ? (
            <EmptyOffers />
          ) : (
            <div className="grid gap-4">
              {data.content.map((offer) => (
                <Card key={offer.id} hover>
                  <CardBody className="flex flex-wrap items-center justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-600">
                        <BookOpenIcon size={19} />
                      </span>
                      <div className="space-y-1.5">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="font-bold text-slate-900">{offer.buyerRequestTitle}</p>
                          <Badge status={offer.status} />
                        </div>
                        <p className="text-xs text-slate-500">
                          {offer.buyerName} ↔ {offer.travellerName}
                        </p>
                        <span className="flex items-center gap-1 text-xs text-slate-500">
                          <DollarIcon size={11} />
                          Total {formatMoney(offer.totalAmount, offer.currency)} · Fee{" "}
                          {formatMoney(offer.travellerFee, offer.currency)}
                        </span>
                      </div>
                    </div>
                    {offer.status === "ACCEPTED" && (
                      <Link to="/bookings">
                        <Button variant="secondary" size="sm">
                          View booking <ArrowRightIcon size={13} />
                        </Button>
                      </Link>
                    )}
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
