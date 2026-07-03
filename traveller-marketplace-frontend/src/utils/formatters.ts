/** Convert an HTML date input value (yyyy-MM-dd) to an ISO Instant string for the backend. */
export function toApiInstant(dateStr: string | undefined | null): string | undefined {
  if (!dateStr) return undefined;
  if (dateStr.includes("T")) return dateStr;
  return `${dateStr}T00:00:00.000Z`;
}

export function formatDate(dateStr: string | undefined | null): string {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

export function formatDateTime(dateStr: string | undefined | null): string {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatMoney(
  amount: number | undefined | null,
  currency = "USD",
): string {
  if (amount == null) return "—";
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  }).format(amount);
}

export function formatRoute(
  fromCity: string,
  fromCountry: string,
  toCity: string,
  toCountry: string,
): string {
  return `${fromCity}, ${fromCountry} → ${toCity}, ${toCountry}`;
}

export function formatStatus(status: string): string {
  return status.replace(/_/g, " ");
}

export function truncate(str: string, max = 80): string {
  return str.length <= max ? str : str.slice(0, max) + "…";
}
