import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/services/adminApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { TextArea } from "@/components/ui/TextArea";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { formatDateTime } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";
import type { NotificationResponse } from "@/types/notification";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const schema = z.object({
  userId: z.string().uuid("Enter a valid User UUID"),
  title: z.string().min(1, "Title required"),
  message: z.string().min(1, "Message required"),
  type: z.string().min(1, "Type required"),
});
type FormValues = z.infer<typeof schema>;

export function AdminNotificationsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [showModal, setShowModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["admin-notifications", page],
    queryFn: () => adminApi.getNotifications(undefined, page, 20).then((r) => r.data.data),
  });

  const sendMutation = useMutation({
    mutationFn: (data: FormValues) => adminApi.sendNotification(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-notifications"] }); setShowModal(false); },
  });

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const columns = [
    {
      key: "title",
      header: "Title",
      render: (n: NotificationResponse) => (
        <div>
          <p className="font-medium text-gray-800">{n.title}</p>
          <p className="text-xs text-gray-400">{n.message.slice(0, 60)}</p>
        </div>
      ),
    },
    { key: "type", header: "Type", render: (n: NotificationResponse) => n.type.replace(/_/g, " ") },
    { key: "channel", header: "Channel" },
    { key: "status", header: "Status" },
    { key: "createdAt", header: "Sent", render: (n: NotificationResponse) => formatDateTime(n.createdAt) },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Notifications</h1>
          <p className="text-sm text-gray-500">Platform notifications management</p>
        </div>
        <Button onClick={() => setShowModal(true)}>Send Notification</Button>
      </div>

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(n) => n.id} isLoading={isLoading} emptyText="No notifications." />
      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      <Modal open={showModal} onClose={() => setShowModal(false)} title="Send Notification">
        <form onSubmit={handleSubmit((d) => sendMutation.mutate(d))} className="space-y-4">
          <Input label="User ID (UUID)" placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" error={errors.userId?.message} {...register("userId")} />
          <Input label="Title" error={errors.title?.message} {...register("title")} />
          <TextArea label="Message" error={errors.message?.message} {...register("message")} />
          <Select
            label="Type"
            options={[
              { value: "SYSTEM", label: "System" },
              { value: "BOOKING_CREATED", label: "Booking Created" },
              { value: "PAYMENT_RECEIVED", label: "Payment Received" },
              { value: "DISPUTE_CREATED", label: "Dispute Created" },
            ]}
            error={errors.type?.message}
            {...register("type")}
          />
          {sendMutation.error && <ErrorMessage message={getErrorMessage(sendMutation.error)} />}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
            <Button type="submit" loading={isSubmitting || sendMutation.isPending}>Send</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
