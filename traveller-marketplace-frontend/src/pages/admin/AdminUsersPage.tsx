import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi, type AdminUserResponse } from "@/services/adminApi";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Pagination } from "@/components/ui/Pagination";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { formatDate } from "@/utils/formatters";
import { getErrorMessage } from "@/api/apiErrorHandler";

export function AdminUsersPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [disableModal, setDisableModal] = useState<AdminUserResponse | null>(null);
  const [disableReason, setDisableReason] = useState("");
  const [changeRoleModal, setChangeRoleModal] = useState<AdminUserResponse | null>(null);
  const [newRole, setNewRole] = useState("");
  const [error, setError] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["admin-users", page, search, roleFilter],
    queryFn: () =>
      adminApi.getUsers({ email: search || undefined, role: roleFilter || undefined }, page, 20).then((r) => r.data.data),
  });

  const disableMutation = useMutation({
    mutationFn: () => adminApi.disableUser(disableModal!.id, disableReason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-users"] }); setDisableModal(null); setDisableReason(""); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const enableMutation = useMutation({
    mutationFn: (userId: string) => adminApi.enableUser(userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-users"] }),
    onError: (err) => setError(getErrorMessage(err)),
  });

  const changeRoleMutation = useMutation({
    mutationFn: () => adminApi.changeRole(changeRoleModal!.id, newRole),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["admin-users"] }); setChangeRoleModal(null); },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const columns = [
    {
      key: "fullName",
      header: "User",
      render: (u: AdminUserResponse) => (
        <div>
          <p className="font-medium text-gray-800">{u.fullName}</p>
          <p className="text-xs text-gray-400">{u.email}</p>
        </div>
      ),
    },
    { key: "role", header: "Role", render: (u: AdminUserResponse) => <StatusBadge status={u.role} /> },
    { key: "accountStatus", header: "Status", render: (u: AdminUserResponse) => <StatusBadge status={u.accountStatus} /> },
    { key: "profileCompleted", header: "Profile", render: (u: AdminUserResponse) => u.profileCompleted ? "✓ Complete" : "Incomplete" },
    { key: "createdAt", header: "Joined", render: (u: AdminUserResponse) => formatDate(u.createdAt) },
    {
      key: "actions",
      header: "",
      render: (u: AdminUserResponse) => (
        <div className="flex gap-1.5">
          {u.accountStatus === "ACTIVE" ? (
            <Button size="sm" variant="ghost" className="text-red-500 hover:bg-red-50" onClick={(e) => { e.stopPropagation(); setError(""); setDisableModal(u); }}>
              Disable
            </Button>
          ) : (
            <Button size="sm" variant="ghost" className="text-emerald-600 hover:bg-emerald-50" onClick={(e) => { e.stopPropagation(); enableMutation.mutate(u.id); }} loading={enableMutation.isPending}>
              Enable
            </Button>
          )}
          <Button size="sm" variant="ghost" onClick={(e) => { e.stopPropagation(); setChangeRoleModal(u); setNewRole(u.role); }}>
            Role
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Users</h1>
        <p className="text-sm text-gray-500">Manage platform users</p>
      </div>

      {/* Filters */}
      <div className="flex gap-3">
        <input
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-100"
          placeholder="Search by email…"
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        />
        <select
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none"
          value={roleFilter}
          onChange={(e) => { setRoleFilter(e.target.value); setPage(0); }}
        >
          <option value="">All Roles</option>
          <option value="BUYER">Buyer</option>
          <option value="TRAVELLER">Traveller</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>

      {error && <ErrorMessage message={error} />}

      <Table columns={columns} data={data?.content ?? []} keyExtractor={(u) => u.id} isLoading={isLoading} emptyText="No users found." />

      <Pagination page={page} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements ?? 0} size={20} onPageChange={setPage} />

      {/* Disable modal */}
      <Modal open={!!disableModal} onClose={() => setDisableModal(null)} title={`Disable ${disableModal?.fullName}`} size="sm">
        <div className="space-y-4">
          <Input label="Reason" placeholder="Reason for disabling..." value={disableReason} onChange={(e) => setDisableReason(e.target.value)} />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setDisableModal(null)}>Cancel</Button>
            <Button variant="danger" size="sm" onClick={() => disableMutation.mutate()} loading={disableMutation.isPending}>Disable User</Button>
          </div>
        </div>
      </Modal>

      {/* Change role modal */}
      <Modal open={!!changeRoleModal} onClose={() => setChangeRoleModal(null)} title={`Change Role for ${changeRoleModal?.fullName}`} size="sm">
        <div className="space-y-4">
          <Select
            label="New Role"
            options={[
              { value: "BUYER", label: "Buyer" },
              { value: "TRAVELLER", label: "Traveller" },
              { value: "ADMIN", label: "Admin" },
            ]}
            value={newRole}
            onChange={(e) => setNewRole(e.target.value)}
          />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setChangeRoleModal(null)}>Cancel</Button>
            <Button size="sm" onClick={() => changeRoleMutation.mutate()} loading={changeRoleMutation.isPending}>Update Role</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
