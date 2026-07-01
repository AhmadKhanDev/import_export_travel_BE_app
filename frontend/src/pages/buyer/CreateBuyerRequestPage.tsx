import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { buyerRequestsApi } from "@/lib/services";
import { fromDatetimeLocal, futureDatetimeLocal } from "@/lib/utils";
import { getErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Alert } from "@/components/ui/Alert";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import type { CreateBuyerRequest } from "@/types";

export function CreateBuyerRequestPage() {
  const navigate = useNavigate();
  const [error, setError] = useState("");

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<CreateBuyerRequest & { neededBeforeLocal: string }>({
    defaultValues: { neededBeforeLocal: futureDatetimeLocal(14) },
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateBuyerRequest) => buyerRequestsApi.create(data).then((r) => r.data.data),
  });

  const onSubmit = async (form: CreateBuyerRequest & { neededBeforeLocal: string }) => {
    setError("");
    try {
      const { neededBeforeLocal, ...rest } = form;
      const payload: CreateBuyerRequest = {
        ...rest,
        estimatedItemPrice: form.estimatedItemPrice ? Number(form.estimatedItemPrice) : undefined,
        neededBefore: neededBeforeLocal ? fromDatetimeLocal(neededBeforeLocal) : undefined,
      };
      const created = await createMutation.mutateAsync(payload);
      navigate(`/buyer/requests/${created.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <h1 className="text-xl font-semibold">New buyer request</h1>
          <p className="text-sm text-slate-500">Describe the item you need from abroad</p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {error && <Alert message={error} />}
            <Input label="Title" required {...register("title")} />
            <Textarea label="Description" rows={3} {...register("description")} />
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Item category" required placeholder="e.g. Electronics" {...register("itemCategory")} />
              <Input label="Brand (optional)" {...register("brand")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Source country" required {...register("sourceCountry")} />
              <Input label="Source city" required {...register("sourceCity")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Destination country" required {...register("destinationCountry")} />
              <Input label="Destination city" required {...register("destinationCity")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Estimated price (£)" type="number" step="0.01" {...register("estimatedItemPrice")} />
              <Input label="Needed before" type="datetime-local" {...register("neededBeforeLocal")} />
            </div>
            <div className="flex gap-3">
              <Button type="submit" loading={isSubmitting}>Create draft</Button>
              <Button type="button" variant="secondary" onClick={() => navigate(-1)}>Cancel</Button>
            </div>
          </form>
        </CardBody>
      </Card>
    </div>
  );
}
