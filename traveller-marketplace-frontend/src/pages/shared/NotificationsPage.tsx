import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notificationApi } from "@/services/notificationApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Pagination } from "@/components/ui/Pagination";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { Bell, BellOff } from "lucide-react";
import { formatDateTime } from "@/utils/formatters";
import type { NotificationResponse } from "@/types/notification";

export function NotificationsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [unreadOnly, setUnreadOnly] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["my-notifications", page, unreadOnly],
    queryFn: () => notificationApi.my(unreadOnly, page, 20).then((r) => r.data.data),
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markAsRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-notifications"] }),
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["my-notifications"] });
      qc.invalidateQueries({ queryKey: ["unread-count"] });
    },
  });

  if (isLoading) return <PageLoader />;

  const notifications: NotificationResponse[] = data?.content ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Notifications</h1>
          <p className="text-sm text-gray-500">{data?.totalElements ?? 0} total notifications</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant={unreadOnly ? "primary" : "secondary"}
            size="sm"
            onClick={() => { setUnreadOnly(!unreadOnly); setPage(0); }}
          >
            {unreadOnly ? "Showing Unread" : "Show Unread"}
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => markAllReadMutation.mutate()}
            loading={markAllReadMutation.isPending}
          >
            Mark all read
          </Button>
        </div>
      </div>

      {notifications.length === 0 ? (
        <EmptyState icon={BellOff} title="No notifications" description="You're all caught up!" />
      ) : (
        <div className="space-y-2">
          {notifications.map((n) => (
            <Card
              key={n.id}
              hoverable
              className={`${n.status === "UNREAD" ? "border-l-4 border-l-primary-500 bg-blue-50/30" : ""}`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div className={`mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${n.status === "UNREAD" ? "bg-primary-100 text-primary-600" : "bg-gray-100 text-gray-400"}`}>
                    <Bell size={14} />
                  </div>
                  <div>
                    <p className={`text-sm font-medium ${n.status === "UNREAD" ? "text-gray-900" : "text-gray-700"}`}>
                      {n.title}
                    </p>
                    <p className="mt-0.5 text-sm text-gray-600">{n.message}</p>
                    <p className="mt-1 text-xs text-gray-400">{formatDateTime(n.createdAt)}</p>
                  </div>
                </div>
                {n.status === "UNREAD" && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => markReadMutation.mutate(n.id)}
                    loading={markReadMutation.isPending}
                    className="shrink-0"
                  >
                    Mark read
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        size={20}
        onPageChange={setPage}
      />
    </div>
  );
}
