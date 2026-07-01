import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useAuth } from "@/store/AuthContext";
import { getErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import {
  GlobeIcon,
  LockIcon,
  UserIcon,
  ShieldIcon,
  CheckCircleIcon,
  ArrowRightIcon,
} from "@/components/Icons";
import type { LoginRequest } from "@/types";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from =
    (location.state as { from?: { pathname: string } })?.from?.pathname ||
    "/dashboard";
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<LoginRequest>();

  const onSubmit = async (data: LoginRequest) => {
    setError("");
    try {
      await login(data);
      navigate(from, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-8rem)] items-center justify-center py-8">
      <div className="w-full max-w-4xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-card-hover">
        <div className="grid lg:grid-cols-2">
          {/* Brand panel */}
          <div className="hidden flex-col justify-between bg-hero-gradient p-10 text-white lg:flex">
            <div>
              <div className="flex items-center gap-2.5">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/20">
                  <GlobeIcon size={18} />
                </span>
                <span className="text-lg font-bold">TB Marketplace</span>
              </div>
              <div className="mt-12 space-y-4">
                <h2 className="text-3xl font-extrabold leading-tight">
                  Welcome back to the marketplace
                </h2>
                <p className="text-white/75 text-sm leading-relaxed">
                  Sign in to manage your requests, trips, and bookings.
                </p>
              </div>
              <div className="mt-10 space-y-3">
                {[
                  { icon: ShieldIcon, text: "Secure escrow payments" },
                  { icon: CheckCircleIcon, text: "KYC-verified travellers" },
                  { icon: LockIcon, text: "6-digit delivery verification" },
                ].map(({ icon: Icon, text }) => (
                  <div key={text} className="flex items-center gap-3 text-sm text-white/80">
                    <Icon size={16} className="shrink-0 text-sky-300" />
                    {text}
                  </div>
                ))}
              </div>
            </div>
            <p className="text-xs text-white/40">
              &copy; {new Date().getFullYear()} Traveller-Buyer Marketplace
            </p>
          </div>

          {/* Form panel */}
          <div className="p-8 sm:p-10">
            <div className="mb-8">
              <h1 className="text-2xl font-extrabold text-slate-900">Sign in</h1>
              <p className="mt-1 text-sm text-slate-500">
                Don't have an account?{" "}
                <Link
                  to="/register"
                  className="font-semibold text-brand-600 hover:text-brand-700"
                >
                  Create one free
                </Link>
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
              {error && <Alert message={error} />}

              <Input
                label="Email address"
                type="email"
                autoComplete="email"
                required
                placeholder="you@example.com"
                icon={<UserIcon size={15} />}
                {...register("email")}
              />

              <Input
                label="Password"
                type="password"
                autoComplete="current-password"
                required
                placeholder="••••••••"
                icon={<LockIcon size={15} />}
                {...register("password")}
              />

              <Button
                type="submit"
                size="lg"
                className="w-full"
                loading={isSubmitting}
              >
                Sign in <ArrowRightIcon size={16} />
              </Button>
            </form>

            <div className="mt-6 rounded-xl bg-slate-50 p-4 text-xs text-slate-500">
              <p className="font-semibold text-slate-700 mb-1">Test accounts</p>
              <p>Register as <b>BUYER</b> or <b>TRAVELLER</b> via the Sign up page.</p>
              <p className="mt-1">Admin: register a user then update role in DB.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
