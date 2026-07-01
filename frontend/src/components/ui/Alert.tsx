import { AlertCircleIcon, CheckCircleIcon, InfoIcon } from "@/components/Icons";

const config = {
  error:   { border: "border-red-200",   bg: "bg-red-50",    text: "text-red-800",    icon: AlertCircleIcon, iconColor: "text-red-500" },
  success: { border: "border-emerald-200", bg: "bg-emerald-50", text: "text-emerald-800", icon: CheckCircleIcon, iconColor: "text-emerald-500" },
  info:    { border: "border-blue-200",  bg: "bg-blue-50",   text: "text-blue-800",   icon: InfoIcon,        iconColor: "text-blue-500" },
  warning: { border: "border-amber-200", bg: "bg-amber-50",  text: "text-amber-800",  icon: AlertCircleIcon, iconColor: "text-amber-500" },
};

export function Alert({
  message,
  type = "error",
}: {
  message: string;
  type?: keyof typeof config;
}) {
  const c = config[type];
  const Icon = c.icon;
  return (
    <div className={`flex items-start gap-3 rounded-xl border px-4 py-3 text-sm ${c.border} ${c.bg} ${c.text} animate-fade-in`}>
      <Icon size={16} className={`mt-0.5 shrink-0 ${c.iconColor}`} />
      <span>{message}</span>
    </div>
  );
}
