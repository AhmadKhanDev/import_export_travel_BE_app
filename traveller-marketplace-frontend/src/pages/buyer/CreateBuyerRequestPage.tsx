import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { buyerRequestApi } from "@/services/buyerRequestApi";
import { Input } from "@/components/ui/Input";
import { TextArea } from "@/components/ui/TextArea";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { ITEM_CATEGORIES, CURRENCIES } from "@/utils/constants";

const schema = z.object({
  title: z.string().min(3, "Title must be at least 3 characters"),
  description: z.string().optional(),
  itemCategory: z.string().min(1, "Category is required"),
  estimatedItemPrice: z.coerce.number().positive("Must be positive").optional().or(z.literal("")),
  currency: z.string().min(1, "Currency is required"),
  travellersReward: z.coerce.number().positive("Reward must be positive"),
  sourceCity: z.string().min(1, "Source city is required"),
  sourceCountry: z.string().min(1, "Source country is required"),
  destinationCity: z.string().min(1, "Destination city is required"),
  destinationCountry: z.string().min(1, "Destination country is required"),
  deadlineDate: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export function CreateBuyerRequestPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data: FormValues) =>
      buyerRequestApi.create({
        ...data,
        estimatedItemPrice: data.estimatedItemPrice === "" ? undefined : Number(data.estimatedItemPrice),
      }),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ["my-requests"] });
      navigate(`/buyer/requests/${res.data.data.id}`);
    },
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { currency: "USD" },
  });

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Create Buyer Request</h1>
        <p className="text-sm text-gray-500">Fill in the details for your item request</p>
      </div>

      {mutation.error && (
        <ErrorMessage message={getErrorMessage(mutation.error)} />
      )}

      <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-4">
        <Card>
          <h2 className="mb-4 font-semibold text-gray-800">Item Details</h2>
          <div className="space-y-4">
            <Input label="Title" placeholder="e.g. iPhone 16 Pro Max 256GB" error={errors.title?.message} {...register("title")} />
            <TextArea label="Description (optional)" placeholder="Additional details about the item..." {...register("description")} />

            <div className="grid grid-cols-2 gap-4">
              <Select
                label="Category"
                error={errors.itemCategory?.message}
                options={ITEM_CATEGORIES.map((c) => ({ value: c, label: c }))}
                placeholder="Select category"
                {...register("itemCategory")}
              />
              <Select
                label="Currency"
                error={errors.currency?.message}
                options={CURRENCIES.map((c) => ({ value: c, label: c }))}
                {...register("currency")}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Estimated Item Price"
                type="number"
                step="0.01"
                placeholder="0.00"
                error={errors.estimatedItemPrice?.message}
                {...register("estimatedItemPrice")}
              />
              <Input
                label="Traveller's Reward"
                type="number"
                step="0.01"
                placeholder="20.00"
                error={errors.travellersReward?.message}
                hint="How much you'll pay the traveller"
                {...register("travellersReward")}
              />
            </div>
          </div>
        </Card>

        <Card>
          <h2 className="mb-4 font-semibold text-gray-800">Delivery Route</h2>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input label="Source City" placeholder="London" error={errors.sourceCity?.message} {...register("sourceCity")} />
              <Input label="Source Country" placeholder="UK" error={errors.sourceCountry?.message} {...register("sourceCountry")} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Input label="Destination City" placeholder="Karachi" error={errors.destinationCity?.message} {...register("destinationCity")} />
              <Input label="Destination Country" placeholder="Pakistan" error={errors.destinationCountry?.message} {...register("destinationCountry")} />
            </div>
            <Input
              label="Deadline Date (optional)"
              type="date"
              error={errors.deadlineDate?.message}
              {...register("deadlineDate")}
            />
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
