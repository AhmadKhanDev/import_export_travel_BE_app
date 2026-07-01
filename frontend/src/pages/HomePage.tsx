import { Link } from "react-router-dom";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/store/AuthContext";
import {
  ShoppingBagIcon,
  PlaneIcon,
  ShieldIcon,
  CheckCircleIcon,
  ArrowRightIcon,
  CreditCardIcon,
  MapPinIcon,
  KeyIcon,
  StarIcon,
} from "@/components/Icons";

const steps = [
  { num: 1, who: "Buyer", title: "Post your request", desc: "Describe the item you need from abroad with location and budget." },
  { num: 2, who: "Traveller", title: "Post your trip", desc: "Share your travel route and available capacity." },
  { num: 3, who: "System", title: "Get matched", desc: "The platform finds the best buyer–traveller match automatically." },
  { num: 4, who: "Traveller", title: "Send an offer", desc: "Traveller sends a detailed offer with pricing to the buyer." },
  { num: 5, who: "Buyer", title: "Accept & pay", desc: "Buyer accepts and pays — funds held safely in escrow." },
  { num: 6, who: "Both", title: "Deliver & verify", desc: "Traveller delivers; buyer confirms with a secure 6-digit code." },
];

const features = [
  {
    icon: ShoppingBagIcon,
    color: "bg-blue-100 text-blue-600",
    title: "For Buyers",
    desc: "Post exactly what you need from abroad. Browse matching travellers, compare offers, and pay securely.",
    cta: "Start buying",
    to: "/register",
  },
  {
    icon: PlaneIcon,
    color: "bg-violet-100 text-violet-600",
    title: "For Travellers",
    desc: "Earn extra on trips you're already taking. Find buyers on your route and deliver conveniently.",
    cta: "Start earning",
    to: "/register",
  },
  {
    icon: ShieldIcon,
    color: "bg-emerald-100 text-emerald-600",
    title: "Safe & Secure",
    desc: "Escrow payments, verified KYC, 6-digit delivery codes, and dispute resolution built in.",
    cta: "See how it works",
    to: "/browse",
  },
];

const stats = [
  { value: "100%", label: "Escrow protected" },
  { value: "KYC", label: "Verified travellers" },
  { value: "6-digit", label: "Delivery verification" },
  { value: "Instant", label: "Payment release" },
];

export function HomePage() {
  const { isLoggedIn } = useAuth();

  return (
    <div className="space-y-20">
      {/* Hero */}
      <section className="relative overflow-hidden rounded-3xl bg-hero-gradient px-6 py-16 text-center text-white sm:px-12 sm:py-24">
        {/* Decorative blobs */}
        <div className="pointer-events-none absolute -top-20 -left-20 h-64 w-64 rounded-full bg-white/5 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-20 -right-20 h-64 w-64 rounded-full bg-brand-400/20 blur-3xl" />

        <div className="relative space-y-4">
          <span className="inline-flex items-center gap-1.5 rounded-full border border-white/20 bg-white/10 px-3 py-1 text-xs font-semibold text-white/90 backdrop-blur-sm">
            <CheckCircleIcon size={13} />
            Secure escrow · Verified travellers · Real-time matching
          </span>

          <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl">
            Shop abroad,
            <br />
            <span className="text-sky-300">delivered by travellers</span>
          </h1>

          <p className="mx-auto max-w-xl text-base text-white/80 sm:text-lg">
            Connect with travellers already heading to your destination. Get
            items you can't buy locally — quickly, safely, and affordably.
          </p>

          <div className="flex flex-wrap justify-center gap-3 pt-2">
            {isLoggedIn ? (
              <Link to="/dashboard">
                <Button size="lg" className="bg-white !text-brand-700 hover:bg-white/90 shadow-lg">
                  Go to Dashboard <ArrowRightIcon size={16} />
                </Button>
              </Link>
            ) : (
              <>
                <Link to="/register">
                  <Button size="lg" className="bg-white !text-brand-700 hover:bg-white/90 shadow-lg">
                    Get started free <ArrowRightIcon size={16} />
                  </Button>
                </Link>
                <Link to="/login">
                  <Button size="lg" className="border border-white/30 !bg-white/10 !text-white hover:!bg-white/20 backdrop-blur-sm">
                    Login
                  </Button>
                </Link>
              </>
            )}
            <Link to="/browse">
              <Button size="lg" className="border border-white/30 !bg-white/10 !text-white hover:!bg-white/20 backdrop-blur-sm">
                Browse listings
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Stats strip */}
      <section className="-mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label} className="rounded-2xl border border-slate-200 bg-white p-5 text-center shadow-card">
            <p className="text-2xl font-extrabold text-brand-700">{s.value}</p>
            <p className="mt-0.5 text-xs text-slate-500">{s.label}</p>
          </div>
        ))}
      </section>

      {/* Features */}
      <section className="space-y-6">
        <div className="text-center">
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
            Built for buyers and travellers
          </h2>
          <p className="mt-2 text-slate-500">Everything you need in one platform</p>
        </div>
        <div className="grid gap-6 sm:grid-cols-3">
          {features.map((f) => {
            const Icon = f.icon;
            return (
              <div
                key={f.title}
                className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-card transition-all duration-200 hover:shadow-card-hover hover:-translate-y-0.5"
              >
                <span className={`inline-flex h-11 w-11 items-center justify-center rounded-xl ${f.color}`}>
                  <Icon size={22} />
                </span>
                <h3 className="mt-4 text-base font-bold text-slate-900">{f.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-600">{f.desc}</p>
                <Link
                  to={f.to}
                  className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand-600 hover:text-brand-700 group-hover:gap-2 transition-all"
                >
                  {f.cta} <ArrowRightIcon size={14} />
                </Link>
              </div>
            );
          })}
        </div>
      </section>

      {/* How it works */}
      <section className="space-y-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
            How it works
          </h2>
          <p className="mt-2 text-slate-500">Six simple steps from request to delivery</p>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {steps.map((step, idx) => {
            const icons = [ShoppingBagIcon, PlaneIcon, MapPinIcon, CreditCardIcon, KeyIcon, StarIcon];
            const IconComp = icons[idx % icons.length];
            const colors = [
              "bg-blue-50 border-blue-200 text-blue-600",
              "bg-violet-50 border-violet-200 text-violet-600",
              "bg-emerald-50 border-emerald-200 text-emerald-600",
              "bg-amber-50 border-amber-200 text-amber-600",
              "bg-sky-50 border-sky-200 text-sky-600",
              "bg-rose-50 border-rose-200 text-rose-600",
            ];
            const roleColors: Record<string, string> = {
              Buyer: "text-blue-600 bg-blue-50",
              Traveller: "text-violet-600 bg-violet-50",
              System: "text-emerald-600 bg-emerald-50",
              Both: "text-slate-600 bg-slate-100",
            };
            return (
              <div key={step.num} className="relative rounded-2xl border border-slate-200 bg-white p-5 shadow-card">
                <div className="flex items-start gap-4">
                  <span className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border ${colors[idx]}`}>
                    <IconComp size={18} />
                  </span>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-400">Step {step.num}</span>
                      <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${roleColors[step.who]}`}>
                        {step.who}
                      </span>
                    </div>
                    <p className="mt-1 font-bold text-slate-900">{step.title}</p>
                    <p className="mt-1 text-sm text-slate-500">{step.desc}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* CTA Banner */}
      {!isLoggedIn && (
        <section className="rounded-3xl bg-brand-gradient p-10 text-center text-white shadow-brand-md">
          <h2 className="text-2xl font-extrabold sm:text-3xl">Ready to get started?</h2>
          <p className="mt-2 text-white/80">
            Join thousands of buyers and travellers on the platform.
          </p>
          <div className="mt-6 flex flex-wrap justify-center gap-3">
            <Link to="/register">
              <Button size="lg" className="bg-white !text-brand-700 hover:bg-white/90">
                Create free account
              </Button>
            </Link>
            <Link to="/browse">
              <Button size="lg" className="border border-white/30 !bg-white/10 !text-white hover:!bg-white/20">
                Browse marketplace
              </Button>
            </Link>
          </div>
        </section>
      )}
    </div>
  );
}
