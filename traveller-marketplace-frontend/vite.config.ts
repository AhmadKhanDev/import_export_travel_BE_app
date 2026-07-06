import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, "");
  const frontendPort = Number(env.FRONTEND_PORT || 5174);

  return {
    plugins: [react()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    server: {
      host: "127.0.0.1",
      port: frontendPort,
      strictPort: true,
    },
    preview: {
      host: "127.0.0.1",
      port: frontendPort,
      strictPort: true,
    },
  };
});
