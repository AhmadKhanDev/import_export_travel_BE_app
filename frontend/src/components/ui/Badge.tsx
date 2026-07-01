const statusConfig: Record<string, { bg: string; text: string; dot: string; label?: string }> = {
  DRAFT:                        { bg: "bg-slate-100",    text: "text-slate-600",   dot: "bg-slate-400" },
  PUBLISHED:                    { bg: "bg-sky-100",      text: "text-sky-700",     dot: "bg-sky-500" },
  MATCHED:                      { bg: "bg-violet-100",   text: "text-violet-700",  dot: "bg-violet-500" },
  BOOKED:                       { bg: "bg-indigo-100",   text: "text-indigo-700",  dot: "bg-indigo-500" },
  COMPLETED:                    { bg: "bg-emerald-100",  text: "text-emerald-700", dot: "bg-emerald-500" },
  CANCELLED:                    { bg: "bg-red-100",      text: "text-red-700",     dot: "bg-red-500" },
  SENT:                         { bg: "bg-amber-100",    text: "text-amber-700",   dot: "bg-amber-500" },
  ACCEPTED:                     { bg: "bg-emerald-100",  text: "text-emerald-700", dot: "bg-emerald-500" },
  REJECTED:                     { bg: "bg-red-100",      text: "text-red-700",     dot: "bg-red-500" },
  EXPIRED:                      { bg: "bg-slate-100",    text: "text-slate-500",   dot: "bg-slate-400" },
  PAYMENT_PENDING:              { bg: "bg-amber-100",    text: "text-amber-700",   dot: "bg-amber-500", label: "Payment Pending" },
  PAYMENT_HELD:                 { bg: "bg-blue-100",     text: "text-blue-700",    dot: "bg-blue-500",  label: "Payment Held" },
  IN_TRANSIT:                   { bg: "bg-violet-100",   text: "text-violet-700",  dot: "bg-violet-500", label: "In Transit" },
  DELIVERED_PENDING_VERIFICATION: { bg: "bg-orange-100", text: "text-orange-700",  dot: "bg-orange-500", label: "Pending Verification" },
  DELIVERED:                    { bg: "bg-teal-100",     text: "text-teal-700",    dot: "bg-teal-500" },
  DISPUTED:                     { bg: "bg-red-100",      text: "text-red-700",     dot: "bg-red-600",   label: "Disputed" },
  SUGGESTED:                    { bg: "bg-sky-100",      text: "text-sky-700",     dot: "bg-sky-500" },
  VIEWED:                       { bg: "bg-slate-100",    text: "text-slate-600",   dot: "bg-slate-400" },
  HELD:                         { bg: "bg-blue-100",     text: "text-blue-700",    dot: "bg-blue-500" },
  RELEASED:                     { bg: "bg-emerald-100",  text: "text-emerald-700", dot: "bg-emerald-500" },
  PENDING:                      { bg: "bg-amber-100",    text: "text-amber-700",   dot: "bg-amber-500" },
  PENDING_OFFER:                { bg: "bg-slate-100",    text: "text-slate-600",   dot: "bg-slate-400", label: "Pending Offer" },
  OFFER_SENT:                   { bg: "bg-amber-100",    text: "text-amber-700",   dot: "bg-amber-500", label: "Offer Sent" },
};

export function Badge({ status }: { status: string }) {
  const cfg = statusConfig[status] ?? { bg: "bg-slate-100", text: "text-slate-600", dot: "bg-slate-400" };
  const label = cfg.label ?? status.replace(/_/g, " ");
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ${cfg.bg} ${cfg.text}`}
    >
      <span className={`h-1.5 w-1.5 shrink-0 rounded-full ${cfg.dot}`} />
      {label}
    </span>
  );
}
