import { Link } from "react-router-dom";
import { useAuth } from "@/store/AuthContext";
import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import {
  ShoppingBagIcon,
  PlaneIcon,
  PackageIcon,
  BookOpenIcon,
  GlobeIcon,
  ArrowRightIcon,
} from "@/components/Icons";

function PlusIcon({ size = 16, className = "" }: { size?: number; className?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" className={className} aria-hidden>
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

const buyerActions = [
  {
    title: "My Requests",
    desc: "Create and manage item requests",
    to: "/buyer/requests",
    icon: ShoppingBagIcon,
    color: "bg-blue-50 text-blue-600",
    border: "border-blue-100",
  },
  {
    title: "Browse Trips",
    desc: "Find travellers going your route",
    to: "/browse?tab=trips",
    icon: GlobeIcon,
    color: "bg-violet-50 text-violet-600",
    border: "border-violet-100",
  },
  {
    title: "Offers",
    desc: "Review offers from travellers",
    to: "/offers",
    icon: BookOpenIcon,
    color: "bg-amber-50 text-amber-600",
    border: "border-amber-100",
  },
  {
    title: "Bookings",
    desc: "Track orders and payments",
    to: "/bookings",
    icon: PackageIcon,
    color: "bg-emerald-50 text-emerald-600",
    border: "border-emerald-100",
  },
];

const travellerActions = [
  {
    title: "My Trips",
    desc: "Post your upcoming travel plans",
    to: "/traveller/trips",
    icon: PlaneIcon,
    color: "bg-violet-50 text-violet-600",
    border: "border-violet-100",
  },
  {
    title: "Browse Requests",
    desc: "Find buyers who need items",
    to: "/browse?tab=requests",
    icon: ShoppingBagIcon,
    color: "bg-blue-50 text-blue-600",
    border: "border-blue-100",
  },
  {
    title: "Offers",
    desc: "Manage sent offers",
    to: "/offers",
    icon: BookOpenIcon,
    color: "bg-amber-50 text-amber-600",
    border: "border-amber-100",
  },
  {
    title: "Bookings",
    desc: "Track deliveries in progress",
    to: "/bookings",
    icon: PackageIcon,
    color: "bg-emerald-50 text-emerald-600",
    border: "border-emerald-100",
  },
];

export function DashboardPage() {
  const { user } = useAuth();
  if (!user) return null;

  const actions = user.role === "TRAVELLER" ? travellerActions : buyerActions;
  const initials = user.fullName
    .split(" ")
    .slice(0, 2)
    .map((n) => n[0])
    .join("")
    .toUpperCase();

  const roleColor =
    user.role === "TRAVELLER"
      ? "bg-violet-100 text-violet-700"
      : user.role === "ADMIN"
      ? "bg-red-100 text-red-700"
      : "bg-blue-100 text-blue-700";

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Welcome banner */}
      <div className="relative overflow-hidden rounded-3xl bg-brand-gradient px-7 py-8 text-white shadow-brand-md">
        <div className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full bg-white/5 blur-2xl" />
        <div className="relative flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-white/20 text-xl font-extrabold text-white">
              {initials}
            </div>
            <div>
              <p className="text-sm text-white/70">Welcome back</p>
              <h1 className="text-2xl font-extrabold">{user.fullName}</h1>
              <div className="mt-1 flex items-center gap-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${roleColor}`}>
                  {user.role}
                </span>
                <span className="text-xs text-white/60">{user.email}</span>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {user.role === "BUYER" && (
              <Link to="/buyer/requests/new">
                <Button className="!bg-white !text-brand-700 hover:!bg-white/90 gap-1.5" size="sm">
                  <PlusIcon size={14} /> New request
                </Button>
              </Link>
            )}
            {user.role === "TRAVELLER" && (
              <Link to="/traveller/trips/new">
                <Button className="!bg-white !text-brand-700 hover:!bg-white/90 gap-1.5" size="sm">
                  <PlusIcon size={14} /> New trip
                </Button>
              </Link>
            )}
            <Link to="/browse">
              <Button className="border border-white/30 !bg-white/10 !text-white hover:!bg-white/20" size="sm">
                Browse
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Quick action cards */}
      <div>
        <h2 className="mb-4 text-lg font-bold text-slate-900">Quick actions</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {actions.map((action) => {
            const Icon = action.icon;
            return (
              <Link key={action.to} to={action.to}>
                <Card hover className="h-full">
                  <CardBody className="flex flex-col gap-3">
                    <span className={`inline-flex h-10 w-10 items-center justify-center rounded-xl ${action.color} border ${action.border}`}>
                      <Icon size={19} />
                    </span>
                    <div>
                      <p className="font-bold text-slate-900">{action.title}</p>
                      <p className="mt-0.5 text-xs text-slate-500">{action.desc}</p>
                    </div>
                    <span className="mt-auto flex items-center gap-1 text-xs font-semibold text-brand-600">
                      Open <ArrowRightIcon size={12} />
                    </span>
                  </CardBody>
                </Card>
              </Link>
            );
          })}
        </div>
      </div>

      {/* Info strip */}
      <div className="rounded-2xl border border-slate-200 bg-slate-50 px-5 py-4 text-sm text-slate-600">
        <span className="font-semibold text-slate-800">Tip: </span>
        {user.role === "BUYER"
          ? "Create a request, publish it, then generate matches to find available travellers."
          : "Post your trip, get it approved via KYC, then publish it and find buyer requests to carry."}
      </div>
    </div>
  );
}
