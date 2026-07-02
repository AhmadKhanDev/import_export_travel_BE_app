import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Mail, Lock } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/common/ErrorMessage";
import { getErrorMessage } from "@/api/apiErrorHandler";
import { useState } from "react";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormValues) => {
    setServerError("");
    try {
      await login(data);
      // Navigate to originally requested page or role dashboard
      const from = (location.state as { from?: { pathname: string } })?.from?.pathname;
      if (from && from !== "/login") {
        navigate(from, { replace: true });
      } else {
        // Will be redirected by router based on role
        navigate("/", { replace: true });
      }
    } catch (err) {
      setServerError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <h2 className="mb-1 text-xl font-bold text-gray-900">Welcome back</h2>
      <p className="mb-6 text-sm text-gray-500">Sign in to your account</p>

      {serverError && (
        <ErrorMessage message={serverError} className="mb-4" />
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Email address"
          type="email"
          placeholder="you@example.com"
          leftIcon={<Mail size={15} />}
          error={errors.email?.message}
          {...register("email")}
        />

        <Input
          label="Password"
          type="password"
          placeholder="••••••••"
          leftIcon={<Lock size={15} />}
          error={errors.password?.message}
          {...register("password")}
        />

        <Button
          type="submit"
          fullWidth
          loading={isSubmitting}
          size="lg"
          className="mt-2"
        >
          Sign in
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-500">
        Don&apos;t have an account?{" "}
        <Link to="/register" className="font-medium text-primary-600 hover:underline">
          Create one
        </Link>
      </p>

      <div className="mt-4 rounded-lg bg-blue-50 p-3 text-xs text-blue-700">
        <strong>Test accounts:</strong>
        <br />
        buyer@test.com / Password@123
        <br />
        traveller@test.com / Password@123
        <br />
        admin@test.com / AdminPass@123
      </div>
    </div>
  );
}
