export type BadgeVariant = "gray" | "blue" | "green" | "yellow" | "red" | "purple" | "orange" | "teal";

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  size?: "sm" | "md";
}

const variants: Record<BadgeVariant, string> = {
  gray:   "bg-gray-100 text-gray-700",
  blue:   "bg-blue-100 text-blue-700",
  green:  "bg-emerald-100 text-emerald-700",
  yellow: "bg-amber-100 text-amber-700",
  red:    "bg-red-100 text-red-700",
  purple: "bg-violet-100 text-violet-700",
  orange: "bg-orange-100 text-orange-700",
  teal:   "bg-teal-100 text-teal-700",
};

const sizes = { sm: "px-2 py-0.5 text-xs", md: "px-2.5 py-1 text-xs" };

export function Badge({ children, variant = "gray", size = "md" }: BadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full font-medium ${variants[variant]} ${sizes[size]}`}>
      {children}
    </span>
  );
}
