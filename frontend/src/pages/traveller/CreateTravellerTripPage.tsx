import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { travellerTripsApi } from "@/lib/services";
import { fromDatetimeLocal, futureDatetimeLocal } from "@/lib/utils";
import { getErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Alert } from "@/components/ui/Alert";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import type { CreateTravellerTrip } from "@/types";

export function CreateTravellerTripPage() {
  const navigate = useNavigate();
  const [error, setError] = useState("");

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<CreateTravellerTrip & { travelDateLocal: string }>({
    defaultValues: { travelDateLocal: futureDatetimeLocal(7) },
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateTravellerTrip) => travellerTripsApi.create(data).then((r) => r.data.data),
  });

  const onSubmit = async (form: CreateTravellerTrip & { travelDateLocal: string }) => {
    setError("");
    try {
      const { travelDateLocal, ...rest } = form;
      const payload: CreateTravellerTrip = {
        ...rest,
        travelDate: fromDatetimeLocal(travelDateLocal),
        availableCapacityKg: form.availableCapacityKg ? Number(form.availableCapacityKg) : undefined,
      };
      const created = await createMutation.mutateAsync(payload);
      navigate(`/traveller/trips/${created.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <Card>
        <CardHeader>
          <h1 className="text-xl font-semibold">New traveller trip</h1>
          <p className="text-sm text-slate-500">Share your upcoming travel route</p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {error && <Alert message={error} />}
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="From country" required {...register("sourceCountry")} />
              <Input label="From city" required {...register("sourceCity")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="To country" required {...register("destinationCountry")} />
              <Input label="To city" required {...register("destinationCity")} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input label="Travel date" type="datetime-local" required {...register("travelDateLocal")} />
              <Input label="Capacity (kg)" type="number" step="0.1" {...register("availableCapacityKg")} />
            </div>
            <Textarea label="Allowed item types" rows={2} placeholder="e.g. Electronics, clothing" {...register("allowedItemTypes")} />
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
