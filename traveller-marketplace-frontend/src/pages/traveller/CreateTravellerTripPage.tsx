import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { travellerTripApi } from "@/services/travellerTripApi";
import { Input } from "@/components/ui/Input";
import { TextArea } from "@/components/ui/TextArea";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";

const schema = z.object({
  sourceCity: z.string().min(1, "Source city required"),
  sourceCountry: z.string().min(1, "Source country required"),
  destinationCity: z.string().min(1, "Destination city required"),
  destinationCountry: z.string().min(1, "Destination country required"),
  travelDate: z.string().min(1, "Travel date required"),
  returnDate: z.string().optional(),
  availableCapacityKg: z.coerce.number().positive().optional().or(z.literal("")),
  notes: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export function CreateTravellerTripPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data: FormValues) =>
      travellerTripApi.create({
        ...data,
        availableCapacityKg:
          data.availableCapacityKg === "" ? undefined : Number(data.availableCapacityKg),
      }),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ["my-trips"] });
      navigate(`/traveller/trips/${res.data.data.id}`);
    },
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Create Trip</h1>
        <p className="text-sm text-gray-500">Add your upcoming trip to find matching buyer requests</p>
      </div>

      {mutation.error && <ErrorMessage message={getErrorMessage(mutation.error)} />}

      <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-4">
        <Card>
          <h2 className="mb-4 font-semibold text-gray-800">Route Details</h2>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input label="Departure City" placeholder="London" error={errors.sourceCity?.message} {...register("sourceCity")} />
              <Input label="Departure Country" placeholder="UK" error={errors.sourceCountry?.message} {...register("sourceCountry")} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Input label="Destination City" placeholder="Karachi" error={errors.destinationCity?.message} {...register("destinationCity")} />
              <Input label="Destination Country" placeholder="Pakistan" error={errors.destinationCountry?.message} {...register("destinationCountry")} />
            </div>
          </div>
        </Card>

        <Card>
          <h2 className="mb-4 font-semibold text-gray-800">Travel Dates & Capacity</h2>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input label="Travel Date" type="date" error={errors.travelDate?.message} {...register("travelDate")} />
              <Input label="Return Date (optional)" type="date" {...register("returnDate")} />
            </div>
            <Input
              label="Available Capacity (kg, optional)"
              type="number"
              step="0.1"
              placeholder="e.g. 5"
              error={errors.availableCapacityKg?.message}
              hint="How much extra weight you can carry"
              {...register("availableCapacityKg")}
            />
            <TextArea label="Notes (optional)" placeholder="Any special notes for buyers..." {...register("notes")} />
          </div>
        </Card>

        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={() => navigate(-1)}>Cancel</Button>
          <Button type="submit" loading={isSubmitting || mutation.isPending}>Save as Draft</Button>
        </div>
      </form>
    </div>
  );
}
