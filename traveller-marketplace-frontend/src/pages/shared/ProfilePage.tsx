import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { Input } from "@/components/ui/Input";
import { TextArea } from "@/components/ui/TextArea";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { profileApi } from "@/services/profileApi";
import { User, Mail, Phone, MapPin } from "lucide-react";
import { formatDate } from "@/utils/formatters";

const schema = z.object({
  fullName: z.string().min(2, "Full name required"),
  bio: z.string().optional(),
  city: z.string().optional(),
  country: z.string().optional(),
  phoneNumber: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const qc = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data: FormValues) => profileApi.update(data),
    onSuccess: () => refreshUser(),
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      fullName: user?.fullName ?? "",
      phoneNumber: user?.phoneNumber ?? "",
    },
  });

  return (
    <div className="mx-auto max-w-xl space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">My Profile</h1>
        <p className="text-sm text-gray-500">View and update your account information</p>
      </div>

      {/* Avatar + info */}
      <Card className="flex items-center gap-5">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-primary-100 text-2xl font-bold text-primary-700">
          {user?.fullName?.[0]?.toUpperCase() ?? "U"}
        </div>
        <div className="flex-1">
          <h2 className="text-lg font-bold text-gray-900">{user?.fullName}</h2>
          <p className="text-sm text-gray-500">{user?.email}</p>
          <div className="mt-2 flex items-center gap-2">
            <StatusBadge status={user?.role ?? ""} />
            <StatusBadge status={user?.accountStatus ?? "ACTIVE"} />
            {user?.profileCompleted && (
              <StatusBadge status="SUCCESS" />
            )}
          </div>
        </div>
      </Card>

      {/* Account details */}
      <Card>
        <h2 className="mb-4 font-semibold text-gray-800">Account Details</h2>
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2 text-gray-600">
            <Mail size={14} /><span>Email:</span><span className="font-medium text-gray-900">{user?.email}</span>
          </div>
          <div className="flex items-center gap-2 text-gray-600">
            <Phone size={14} /><span>Phone:</span><span className="font-medium text-gray-900">{user?.phoneNumber ?? "—"}</span>
          </div>
          <div className="flex items-center gap-2 text-gray-600">
            <User size={14} /><span>Joined:</span><span className="font-medium text-gray-900">{formatDate(user?.createdAt)}</span>
          </div>
        </div>
      </Card>

      {/* Update form */}
      <Card>
        <h2 className="mb-4 font-semibold text-gray-800">Update Profile</h2>

        {mutation.error && <ErrorMessage message={getErrorMessage(mutation.error)} className="mb-4" />}
        {mutation.isSuccess && <ErrorMessage message="Profile updated!" type="success" className="mb-4" />}

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-4">
          <Input
            label="Full Name"
            leftIcon={<User size={14} />}
            error={errors.fullName?.message}
            {...register("fullName")}
          />
          <Input
            label="Phone Number"
            leftIcon={<Phone size={14} />}
            {...register("phoneNumber")}
          />
          <div className="grid grid-cols-2 gap-4">
            <Input label="City" leftIcon={<MapPin size={14} />} {...register("city")} />
            <Input label="Country" {...register("country")} />
          </div>
          <TextArea label="Bio" placeholder="Tell us about yourself..." rows={3} {...register("bio")} />
          <Button type="submit" loading={isSubmitting || mutation.isPending}>Save Changes</Button>
        </form>
      </Card>
    </div>
  );
}
