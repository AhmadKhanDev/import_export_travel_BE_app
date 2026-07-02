import { Bell, Menu, LogOut, User } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import { useQuery } from "@tanstack/react-query";
import { notificationApi } from "@/services/notificationApi";

interface TopbarProps {
  onMenuClick: () => void;
}

export function Topbar({ onMenuClick }: TopbarProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const { data: unreadData } = useQuery({
    queryKey: ["unread-count"],
    queryFn: () => notificationApi.unreadCount().then((r) => r.data.data),
    refetchInterval: 30_000,
    enabled: !!user,
  });

  const unreadCount = unreadData?.unreadCount ?? 0;

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const notifPath =
    user?.role === "BUYER"
      ? "/buyer/notifications"
      : user?.role === "TRAVELLER"
        ? "/traveller/notifications"
        : "/admin/notifications";

  const profilePath =
    user?.role === "BUYER"
      ? "/buyer/profile"
      : user?.role === "TRAVELLER"
        ? "/traveller/profile"
        : "/admin/profile";

  return (
    <header className="flex h-16 shrink-0 items-center justify-between border-b border-gray-200 bg-white px-4 shadow-sm">
      <button
        onClick={onMenuClick}
        className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 lg:hidden"
      >
        <Menu size={20} />
      </button>

      <div className="flex flex-1 items-center justify-end gap-2">
        {/* Notifications bell */}
        <Link
          to={notifPath}
          className="relative rounded-lg p-2 text-gray-500 hover:bg-gray-100"
        >
          <Bell size={20} />
          {unreadCount > 0 && (
            <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
              {unreadCount > 9 ? "9+" : unreadCount}
            </span>
          )}
        </Link>

        {/* Profile */}
        <Link
          to={profilePath}
          className="flex items-center gap-2 rounded-lg px-3 py-2 hover:bg-gray-100"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-xs font-bold text-primary-700">
            {user?.fullName?.[0]?.toUpperCase() ?? "U"}
          </div>
          <div className="hidden text-left sm:block">
            <p className="text-sm font-medium text-gray-800 leading-tight">{user?.fullName}</p>
            <p className="text-xs text-gray-400">{user?.role}</p>
          </div>
        </Link>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 hover:text-red-600"
          title="Logout"
        >
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}
