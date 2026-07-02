import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { kycApi } from "@/services/kycApi";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { FileCheck, CheckCircle, Clock, XCircle } from "lucide-react";

const schema = z.object({
  documentType: z.string().min(1, "Document type required"),
  documentNumber: z.string().min(3, "Document number required"),
  documentUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
});

type FormValues = z.infer<typeof schema>;

const DOC_TYPES = [
  { value: "PASSPORT", label: "Passport" },
  { value: "NATIONAL_ID", label: "National ID" },
  { value: "DRIVING_LICENSE", label: "Driving License" },
];

export function KycPage() {
  const qc = useQueryClient();

  const { data: kycData, isLoading } = useQuery({
    queryKey: ["my-kyc"],
    queryFn: () => kycApi.getMyStatus().then((r) => r.data.data).catch(() => null),
  });

  const mutation = useMutation({
    mutationFn: (data: FormValues) => kycApi.submit({
      ...data,
      documentUrl: data.documentUrl || undefined,
    }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["my-kyc"] }),
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  if (isLoading) return <PageLoader />;

  const kycStatus = kycData?.status;
  const isApproved = kycStatus === "APPROVED";
  const isPending = kycStatus === "PENDING_REVIEW";

  return (
    <div className="mx-auto max-w-xl space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">KYC Verification</h1>
        <p className="text-sm text-gray-500">Identity verification required to publish trips</p>
      </div>

      {/* Current status */}
      {kycStatus && (
        <Card>
          <div className="flex items-center gap-4">
            <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${
              isApproved ? "bg-emerald-100" : isPending ? "bg-amber-100" : "bg-red-100"
            }`}>
              {isApproved ? (
                <CheckCircle size={24} className="text-emerald-600" />
              ) : isPending ? (
                <Clock size={24} className="text-amber-600" />
              ) : (
                <XCircle size={24} className="text-red-600" />
              )}
            </div>
            <div>
              <p className="font-medium text-gray-900">KYC Status</p>
              <div className="mt-1">
                <StatusBadge status={kycStatus} />
              </div>
              <p className="mt-1 text-xs text-gray-500">
                {isApproved
                  ? "Your identity has been verified. You can publish trips and send offers."
                  : isPending
                    ? "Your documents are under review. Please wait for admin approval."
                    : "Your KYC was rejected. Please resubmit with valid documents."}
              </p>
            </div>
          </div>
        </Card>
      )}

      {!isApproved && !isPending && (
        <Card>
          <div className="mb-4 flex items-center gap-2">
            <FileCheck size={18} className="text-primary-600" />
            <h2 className="font-semibold text-gray-900">Submit KYC Documents</h2>
          </div>

          {mutation.error && (
            <ErrorMessage message={getErrorMessage(mutation.error)} className="mb-4" />
          )}

          {mutation.isSuccess && (
            <ErrorMessage
              message="KYC submitted successfully! Please wait for admin approval."
              type="success"
              className="mb-4"
            />
          )}

          <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-4">
            <Select
              label="Document Type"
              options={DOC_TYPES}
              placeholder="Select document type"
              error={errors.documentType?.message}
              {...register("documentType")}
            />

            <Input
              label="Document Number"
              placeholder="e.g. P123456789"
              error={errors.documentNumber?.message}
              {...register("documentNumber")}
            />

            <Input
              label="Document URL (optional)"
              type="url"
              placeholder="https://example.com/document.jpg"
              error={errors.documentUrl?.message}
              hint="Link to a scan or photo of your document"
              {...register("documentUrl")}
            />

            <Button type="submit" fullWidth loading={isSubmitting || mutation.isPending}>
              Submit KYC
            </Button>
          </form>
        </Card>
      )}
    </div>
  );
}
