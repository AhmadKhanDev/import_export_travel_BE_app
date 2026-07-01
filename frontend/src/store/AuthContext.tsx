import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getCurrentUser,
  isAuthenticated,
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
  saveTokens,
} from "@/lib/auth";
import type { LoginRequest, RegisterRequest, User } from "@/types";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  isLoggedIn: boolean;
  login: (data: LoginRequest) => Promise<User>;
  register: (data: RegisterRequest) => Promise<User>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [loggedIn, setLoggedIn] = useState(isAuthenticated());

  const { data: user, isLoading } = useQuery({
    queryKey: ["me"],
    queryFn: getCurrentUser,
    enabled: loggedIn,
    retry: false,
  });

  useEffect(() => {
    if (!isLoading && loggedIn && !user) {
      setLoggedIn(false);
    }
  }, [isLoading, loggedIn, user]);

  const handleAuth = async (authFn: () => Promise<{ accessToken: string; refreshToken: string; user: User }>) => {
    const res = await authFn();
    saveTokens(res.accessToken, res.refreshToken);
    setLoggedIn(true);
    queryClient.setQueryData(["me"], res.user);
    return res.user;
  };

  const value: AuthContextValue = {
    user: user ?? null,
    isLoading: loggedIn && isLoading,
    isLoggedIn: loggedIn && !!user,
    login: (data) => handleAuth(() => loginApi(data)),
    register: (data) => handleAuth(() => registerApi(data)),
    logout: async () => {
      await logoutApi();
      setLoggedIn(false);
      queryClient.clear();
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
