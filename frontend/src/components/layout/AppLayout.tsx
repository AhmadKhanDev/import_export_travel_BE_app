import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "@/store/AuthContext";
import { Button } from "@/components/ui/Button";
import {
  GlobeIcon,
  MenuIcon,
  XIcon,
  LogOutIcon,
  UserIcon,
  BookOpenIcon,
  PackageIcon,
  PlaneIcon,
  ShoppingBagIcon,
} from "@/components/Icons";

const linkBase =
  "flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all duration-150";
const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `${linkBase} ${
    isActive
      ? "bg-brand-50 text-brand-700"
      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
  }`;

export function AppLayout() {
  const { user, logout, isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  const initials = user?.fullName
    ? user.fullName
        .split(" ")
        .slice(0, 2)
        .map((n) => n[0])
        .join("")
        .toUpperCase()
    : "U";

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/95 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center gap-2.5 font-bold text-slate-900 hover:opacity-80 transition-opacity"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient text-white shadow-brand-sm">
              <GlobeIcon size={18} />
            </span>
            <span className="hidden sm:inline tracking-tight">
              <span className="text-brand-700">TB</span> Marketplace
            </span>
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden items-center gap-0.5 md:flex">
            <NavLink to="/browse" className={navLinkClass}>
              <GlobeIcon size={15} />
              Browse
            </NavLink>
            {isLoggedIn && (
              <>
                <NavLink to="/dashboard" className={navLinkClass}>
                  <UserIcon size={15} />
                  Dashboard
                </NavLink>
                <NavLink to="/bookings" className={navLinkClass}>
                  <PackageIcon size={15} />
                  Bookings
                </NavLink>
                <NavLink to="/offers" className={navLinkClass}>
                  <BookOpenIcon size={15} />
                  Offers
                </NavLink>
                {user?.role === "BUYER" && (
                  <NavLink to="/buyer/requests" className={navLinkClass}>
                    <ShoppingBagIcon size={15} />
                    My Requests
                  </NavLink>
                )}
                {user?.role === "TRAVELLER" && (
                  <NavLink to="/traveller/trips" className={navLinkClass}>
                    <PlaneIcon size={15} />
                    My Trips
                  </NavLink>
                )}
              </>
            )}
          </nav>

          {/* Right side */}
          <div className="flex items-center gap-2">
            {isLoggedIn && user ? (
              <>
                <div className="hidden sm:flex items-center gap-2.5">
                  <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-xs font-bold text-white">
                    {initials}
                  </div>
                  <div className="hidden lg:block text-right">
                    <p className="text-xs font-semibold text-slate-800 leading-none">{user.fullName}</p>
                    <p className="text-xs text-slate-400 mt-0.5">{user.role}</p>
                  </div>
                </div>
                <Button variant="ghost" size="sm" onClick={handleLogout} className="gap-1.5">
                  <LogOutIcon size={15} />
                  <span className="hidden sm:inline">Logout</span>
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">Login</Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">Sign up</Button>
                </Link>
              </>
            )}
            {/* Mobile menu toggle */}
            <button
              className="flex items-center justify-center rounded-xl p-2 text-slate-600 hover:bg-slate-100 md:hidden"
              onClick={() => setMobileOpen((v) => !v)}
              aria-label="Toggle menu"
            >
              {mobileOpen ? <XIcon size={20} /> : <MenuIcon size={20} />}
            </button>
          </div>
        </div>

        {/* Mobile nav */}
        {mobileOpen && (
          <div className="border-t border-slate-100 bg-white px-4 pb-4 pt-2 md:hidden animate-slide-up">
            <div className="flex flex-col gap-1">
              <NavLink to="/browse" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                <GlobeIcon size={15} /> Browse
              </NavLink>
              {isLoggedIn && (
                <>
                  <NavLink to="/dashboard" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                    <UserIcon size={15} /> Dashboard
                  </NavLink>
                  <NavLink to="/bookings" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                    <PackageIcon size={15} /> Bookings
                  </NavLink>
                  <NavLink to="/offers" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                    <BookOpenIcon size={15} /> Offers
                  </NavLink>
                  {user?.role === "BUYER" && (
                    <NavLink to="/buyer/requests" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                      <ShoppingBagIcon size={15} /> My Requests
                    </NavLink>
                  )}
                  {user?.role === "TRAVELLER" && (
                    <NavLink to="/traveller/trips" className={navLinkClass} onClick={() => setMobileOpen(false)}>
                      <PlaneIcon size={15} /> My Trips
                    </NavLink>
                  )}
                  <button
                    onClick={() => { handleLogout(); setMobileOpen(false); }}
                    className={`${linkBase} text-red-600 hover:bg-red-50 w-full justify-start`}
                  >
                    <LogOutIcon size={15} /> Logout
                  </button>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 animate-fade-in">
        <Outlet />
      </main>

      <footer className="mt-8 border-t border-slate-200 bg-white">
        <div className="mx-auto max-w-6xl px-4 py-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-sm text-slate-500">
          <div className="flex items-center gap-2">
            <span className="flex h-6 w-6 items-center justify-center rounded-lg bg-brand-gradient text-white">
              <GlobeIcon size={12} />
            </span>
            <span className="font-medium text-slate-700">TB Marketplace</span>
          </div>
          <p>&copy; {new Date().getFullYear()} Traveller-Buyer Marketplace. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
