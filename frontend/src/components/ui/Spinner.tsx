export function Spinner({ className = "" }: { className?: string }) {
  return (
    <div className={`flex flex-col items-center justify-center gap-3 py-16 ${className}`}>
      <div className="relative h-10 w-10">
        <div className="absolute inset-0 animate-spin rounded-full border-4 border-brand-100 border-t-brand-600" />
      </div>
      <p className="text-sm text-slate-400">Loading…</p>
    </div>
  );
}
