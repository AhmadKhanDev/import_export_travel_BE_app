import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
  ShoppingBagIcon,
  PlaneIcon,
  ArrowRightIcon,
  CheckCircleIcon,
} from "@/components/Icons";
import type { RegisterRequest, Role } from "@/types";

export function RegisterPage() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [selectedRole, setSelectedRole] = useState<Role>("BUYER");

  const {
    register,
    handleSubmit,
    setValue,
    formState: { isSubmitting },
  } = useForm<RegisterRequest>({ defaultValues: { role: "BUYER" as Role } });

  const pickRole = (role: Role) => {
    setSelectedRole(role);
    setValue("role", role);
  };

  const onSubmit = async (data: RegisterRequest) => {
    setError("");
    try {
      await registerUser(data);
      navigate("/dashboard", { replace: true });
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
                  Join the community
                </h2>
                <p className="text-white/75 text-sm leading-relaxed">
                  Whether you need items from abroad or you travel regularly —
                  there's a place for you here.
                </p>
              </div>
              <div className="mt-10 space-y-4">
                {[
                  {
                    icon: ShoppingBagIcon,
                    title: "As a Buyer",
                    desc: "Post requests and receive offers from verified travellers.",
                  },
                  {
                    icon: PlaneIcon,
                    title: "As a Traveller",
                    desc: "Post your trips and earn by carrying items.",
                  },
                ].map(({ icon: Icon, title, desc }) => (
                  <div key={title} className="flex gap-3">
                    <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-white/15">
                      <Icon size={16} />
                    </span>
                    <div>
                      <p className="text-sm font-semibold">{title}</p>
                      <p className="text-xs text-white/65">{desc}</p>
                    </div>
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
            <div className="mb-6">
              <h1 className="text-2xl font-extrabold text-slate-900">Create account</h1>
              <p className="mt-1 text-sm text-slate-500">
                Already have one?{" "}
                <Link
                  to="/login"
                  className="font-semibold text-brand-600 hover:text-brand-700"
                >
                  Sign in
                </Link>
              </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              {error && <Alert message={error} />}

              {/* Role picker */}
              <div className="space-y-1.5">
                <p className="text-sm font-medium text-slate-700">I want to</p>
                <div className="grid grid-cols-2 gap-3">
                  {(
                    [
                      { role: "BUYER" as Role, icon: ShoppingBagIcon, label: "Buy items\nfrom abroad" },
                      { role: "TRAVELLER" as Role, icon: PlaneIcon, label: "Carry items\non my trips" },
                    ] as const
                  ).map(({ role, icon: Icon, label }) => (
                    <button
                      key={role}
                      type="button"
                      onClick={() => pickRole(role)}
                      className={`flex flex-col items-center gap-2 rounded-xl border-2 p-4 text-sm font-semibold transition-all ${
                        selectedRole === role
                          ? "border-brand-500 bg-brand-50 text-brand-700"
                          : "border-slate-200 text-slate-600 hover:border-slate-300 hover:bg-slate-50"
                      }`}
                    >
                      <Icon size={22} />
                      {label.split("\n").map((l, i) => <span key={i}>{l}</span>)}
                      {selectedRole === role && (
                        <CheckCircleIcon size={14} className="text-brand-500" />
                      )}
                    </button>
                  ))}
                </div>
              </div>

              <Input
                label="Full name"
                required
                placeholder="Ahmad Khan"
                icon={<UserIcon size={15} />}
                {...register("fullName")}
              />
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
                label="Phone number"
                placeholder="+44 7700 900000 (optional)"
                {...register("phoneNumber")}
              />
              <Input
                label="Password"
                type="password"
                autoComplete="new-password"
                required
                minLength={8}
                placeholder="Min. 8 characters"
                icon={<LockIcon size={15} />}
                hint="At least 8 characters"
                {...register("password", { minLength: 8 })}
              />

              <input type="hidden" {...register("role")} value={selectedRole} />

              <Button
                type="submit"
                size="lg"
                className="w-full"
                loading={isSubmitting}
              >
                Create account <ArrowRightIcon size={16} />
              </Button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
