import { AlertCircle, CheckCircle, Info, AlertTriangle } from "lucide-react";
import { type ReactNode } from "react";

type AlertType = "error" | "success" | "info" | "warning";

interface ErrorMessageProps {
  message: string | ReactNode;
  type?: AlertType;
  className?: string;
}

const configs: Record<AlertType, { icon: typeof AlertCircle; classes: string }> = {
  error:   { icon: AlertCircle,   classes: "border-red-200 bg-red-50 text-red-800" },
  success: { icon: CheckCircle,   classes: "border-emerald-200 bg-emerald-50 text-emerald-800" },
  info:    { icon: Info,          classes: "border-blue-200 bg-blue-50 text-blue-800" },
  warning: { icon: AlertTriangle, classes: "border-amber-200 bg-amber-50 text-amber-800" },
};

export function ErrorMessage({ message, type = "error", className = "" }: ErrorMessageProps) {
  const { icon: Icon, classes } = configs[type];
  return (
    <div className={`flex items-start gap-3 rounded-xl border p-3 text-sm ${classes} ${className}`}>
      <Icon size={16} className="mt-0.5 shrink-0" />
      <span>{message}</span>
    </div>
  );
}
