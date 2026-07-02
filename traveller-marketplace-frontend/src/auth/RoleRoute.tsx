import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import type { Role } from "@/types/auth";

interface RoleRouteProps {
  roles: Role[];
}

export function RoleRoute({ roles }: RoleRouteProps) {
  const { user } = useAuth();

  if (!user || !roles.includes(user.role)) {
    // Redirect to their proper dashboard
    if (user?.role === "BUYER") return <Navigate to="/buyer/dashboard" replace />;
    if (user?.role === "TRAVELLER") return <Navigate to="/traveller/dashboard" replace />;
    if (user?.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
