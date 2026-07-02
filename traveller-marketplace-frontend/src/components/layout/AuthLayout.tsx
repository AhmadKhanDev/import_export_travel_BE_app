import { Outlet } from "react-router-dom";
import { Globe } from "lucide-react";

export function AuthLayout() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-900 via-primary-700 to-primary-500 p-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-white shadow-lg">
            <Globe size={28} className="text-primary-600" />
          </div>
          <h1 className="text-2xl font-bold text-white">Traveller Marketplace</h1>
          <p className="mt-1 text-sm text-white/70">
            Connect buyers and travellers worldwide
          </p>
        </div>
        <div className="rounded-2xl bg-white p-6 shadow-2xl">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
