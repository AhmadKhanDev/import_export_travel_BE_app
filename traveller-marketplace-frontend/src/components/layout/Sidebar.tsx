import { NavLink } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import {
  LayoutDashboard,
  ShoppingBag,
  PlusCircle,
  Package,
  Bell,
  MessageSquare,
  Star,
  AlertTriangle,
  User,
  Plane,
  GitMerge,
  CreditCard,
  Users,
  FileCheck,
  FileText,
  DollarSign,
  ClipboardList,
  Globe,
  LogOut,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

interface NavItem {
  label: string;
  to: string;
  icon: typeof LayoutDashboard;
}

const buyerNav: NavItem[] = [
  { label: "Dashboard",     to: "/buyer/dashboard",       icon: LayoutDashboard },
  { label: "My Requests",   to: "/buyer/requests",        icon: ShoppingBag },
  { label: "Create Request",to: "/buyer/requests/create", icon: PlusCircle },
  { label: "Offers",        to: "/buyer/offers",          icon: FileText },
  { label: "Bookings",      to: "/buyer/bookings",        icon: Package },
  { label: "Reviews",       to: "/shared/reviews",        icon: Star },
  { label: "Disputes",      to: "/shared/disputes",       icon: AlertTriangle },
  { label: "Chat",          to: "/shared/chat",           icon: MessageSquare },
  { label: "Notifications", to: "/buyer/notifications",   icon: Bell },
  { label: "Profile",       to: "/buyer/profile",         icon: User },
];

const travellerNav: NavItem[] = [
  { label: "Dashboard",     to: "/traveller/dashboard",       icon: LayoutDashboard },
  { label: "KYC",           to: "/traveller/kyc",             icon: FileCheck },
  { label: "My Trips",      to: "/traveller/trips",           icon: Plane },
  { label: "Create Trip",   to: "/traveller/trips/create",    icon: PlusCircle },
  { label: "Matches",       to: "/traveller/matches",         icon: GitMerge },
  { label: "Bookings",      to: "/traveller/bookings",        icon: Package },
  { label: "Reviews",       to: "/shared/reviews",            icon: Star },
  { label: "Disputes",      to: "/shared/disputes",           icon: AlertTriangle },
  { label: "Chat",          to: "/shared/chat",               icon: MessageSquare },
  { label: "Notifications", to: "/traveller/notifications",   icon: Bell },
  { label: "Profile",       to: "/traveller/profile",         icon: User },
];

const adminNav: NavItem[] = [
  { label: "Dashboard",      to: "/admin/dashboard",       icon: LayoutDashboard },
  { label: "Users",          to: "/admin/users",           icon: Users },
  { label: "KYC",            to: "/admin/kyc",             icon: FileCheck },
  { label: "Buyer Requests", to: "/admin/buyer-requests",  icon: ShoppingBag },
  { label: "Traveller Trips",to: "/admin/traveller-trips", icon: Plane },
  { label: "Bookings",       to: "/admin/bookings",        icon: Package },
  { label: "Payments",       to: "/admin/payments",        icon: DollarSign },
  { label: "Disputes",       to: "/admin/disputes",        icon: AlertTriangle },
  { label: "Notifications",  to: "/admin/notifications",   icon: Bell },
  { label: "Audit Logs",     to: "/admin/audit-logs",      icon: ClipboardList },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
    isActive
      ? "bg-primary-600 text-white shadow-sm"
      : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
  }`;

export function Sidebar({ open, onClose }: SidebarProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const nav =
    user?.role === "BUYER"
      ? buyerNav
      : user?.role === "TRAVELLER"
        ? travellerNav
        : adminNav;

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-40 bg-black/30 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar panel */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-gray-200 bg-white transition-transform duration-200 lg:static lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Logo */}
        <div className="flex h-16 shrink-0 items-center gap-2.5 border-b border-gray-200 px-5">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-600 text-white">
            <Globe size={16} />
          </div>
          <span className="text-sm font-bold text-gray-900">TB Marketplace</span>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to.endsWith("dashboard")}
              className={linkClass}
              onClick={onClose}
            >
              <item.icon size={17} className="shrink-0" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Logout button at bottom */}
        <div className="shrink-0 border-t border-gray-200 p-3">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
          >
            <LogOut size={17} className="shrink-0" />
            Logout
          </button>
        </div>
      </aside>
    </>
  );
}
