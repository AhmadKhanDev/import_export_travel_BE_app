import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link, useNavigate } from "react-router-dom";
import { Mail, Lock, User, Phone, ShoppingBag, Plane } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { useState } from "react";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Must contain uppercase")
    .regex(/[0-9]/, "Must contain a number")
    .regex(/[^A-Za-z0-9]/, "Must contain a special character"),
  fullName: z.string().min(2, "Full name is required"),
  phoneNumber: z.string().optional(),
  role: z.enum(["BUYER", "TRAVELLER"]),
});

type FormValues = z.infer<typeof schema>;

export function RegisterPage() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");

  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { role: "BUYER" },
  });

  const selectedRole = watch("role");

  const onSubmit = async (data: FormValues) => {
    setServerError("");
    try {
      await registerUser(data);
      navigate("/", { replace: true });
    } catch (err) {
      setServerError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <h2 className="mb-1 text-xl font-bold text-gray-900">Create account</h2>
      <p className="mb-6 text-sm text-gray-500">Join as a buyer or traveller</p>

      {serverError && (
        <ErrorMessage message={serverError} className="mb-4" />
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {/* Role picker */}
        <div>
          <label className="mb-2 block text-sm font-medium text-gray-700">I am a</label>
          <Controller
            name="role"
            control={control}
            render={({ field }) => (
              <div className="grid grid-cols-2 gap-3">
                {(["BUYER", "TRAVELLER"] as const).map((r) => (
                  <button
                    key={r}
                    type="button"
                    onClick={() => field.onChange(r)}
                    className={`flex items-center gap-2.5 rounded-xl border-2 p-3 text-sm font-medium transition-all ${
                      field.value === r
                        ? "border-primary-600 bg-primary-50 text-primary-700"
                        : "border-gray-200 text-gray-600 hover:border-gray-300"
                    }`}
                  >
                    {r === "BUYER" ? <ShoppingBag size={16} /> : <Plane size={16} />}
                    {r.charAt(0) + r.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
            )}
          />
          {errors.role && (
            <p className="mt-1 text-xs text-red-600">{errors.role.message}</p>
          )}
        </div>

        <Input
          label="Full name"
          placeholder="Ahmad Khan"
          leftIcon={<User size={15} />}
          error={errors.fullName?.message}
          {...register("fullName")}
        />

        <Input
          label="Email address"
          type="email"
          placeholder="you@example.com"
          leftIcon={<Mail size={15} />}
          error={errors.email?.message}
          {...register("email")}
        />

        <Input
          label="Phone number (optional)"
          type="tel"
          placeholder="+44 7700 900000"
          leftIcon={<Phone size={15} />}
          error={errors.phoneNumber?.message}
          {...register("phoneNumber")}
        />

        <Input
          label="Password"
          type="password"
          placeholder="Min 8 chars, 1 uppercase, 1 number, 1 special"
          leftIcon={<Lock size={15} />}
          error={errors.password?.message}
          hint="e.g. Password@123"
          {...register("password")}
        />

        <Button
          type="submit"
          fullWidth
          loading={isSubmitting}
          size="lg"
        >
          Create {selectedRole === "BUYER" ? "Buyer" : "Traveller"} account
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-500">
        Already have an account?{" "}
        <Link to="/login" className="font-medium text-primary-600 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
