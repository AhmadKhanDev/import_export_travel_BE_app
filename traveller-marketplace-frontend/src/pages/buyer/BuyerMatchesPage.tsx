import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { matchingApi } from "@/services/matchingApi";
import { Card } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { GitMerge, ShoppingBag } from "lucide-react";

export function BuyerMatchesPage() {
  const { data: requestsData, isLoading } = useQuery({
    queryKey: ["my-requests-published"],
    queryFn: () => buyerRequestApi.my({ status: "PUBLISHED", size: 50 }).then((r) => r.data.data),
  });

  if (isLoading) return <PageLoader />;

  const requests = requestsData?.content ?? [];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Matches</h1>
        <p className="text-sm text-gray-500">View matches for your published requests</p>
      </div>

      {requests.length === 0 ? (
        <EmptyState
          icon={GitMerge}
          title="No published requests"
          description="Publish a buyer request to see matches."
        />
      ) : (
        requests.map((req) => (
          <RequestMatchCard key={req.id} requestId={req.id} requestTitle={req.title} requestStatus={req.status} />
        ))
      )}
    </div>
  );
}

function RequestMatchCard({
  requestId,
  requestTitle,
  requestStatus,
}: {
  requestId: string;
  requestTitle: string;
  requestStatus: string;
}) {
  const navigate = useNavigate();

  const { data: matchesData, isLoading } = useQuery({
    queryKey: ["matches-by-request", requestId],
    queryFn: () => matchingApi.getByBuyerRequest(requestId).then((r) => r.data.data),
  });

  return (
    <Card>
      <div className="mb-3 flex items-center justify-between">
        <h2 className="font-medium text-gray-900">{requestTitle}</h2>
        <StatusBadge status={requestStatus} />
      </div>
      {isLoading ? (
        <p className="text-sm text-gray-400">Loading matches…</p>
      ) : matchesData?.content?.length === 0 ? (
        <p className="text-sm text-gray-400">No matches yet. Go to the request detail to generate matches.</p>
      ) : (
        <div className="space-y-2">
          {matchesData?.content?.map((m) => (
            <div
              key={m.id}
              className="flex cursor-pointer items-center justify-between rounded-lg bg-gray-50 px-3 py-2 hover:bg-gray-100"
              onClick={() => navigate(`/buyer/requests/${requestId}`)}
            >
              <div>
                <p className="text-sm font-medium text-gray-800">{m.travellerName}</p>
                <p className="text-xs text-gray-500">Score: {Math.round(m.matchScore * 100)}%</p>
              </div>
              <StatusBadge status={m.status} size="sm" />
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
