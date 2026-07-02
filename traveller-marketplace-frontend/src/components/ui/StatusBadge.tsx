import { Badge } from "./Badge";
import type { BadgeVariant } from "./Badge";

type Variant = BadgeVariant;

const statusVariantMap: Record<string, Variant> = {
  // Buyer Request / Traveller Trip
  DRAFT: "gray",
  PUBLISHED: "blue",
  MATCHED: "purple",
  BOOKED: "teal",
  // Booking
  ACCEPTED: "blue",
  PAYMENT_PENDING: "yellow",
  PAYMENT_HELD: "teal",
  IN_TRANSIT: "purple",
  DELIVERED_PENDING_VERIFICATION: "orange",
  DELIVERED: "teal",
  COMPLETED: "green",
  CANCELLED: "red",
  DISPUTED: "red",
  // Offer
  SENT: "blue",
  REJECTED: "red",
  EXPIRED: "gray",
  // Payment
  PENDING: "yellow",
  HELD: "teal",
  RELEASED: "green",
  REFUNDED: "orange",
  FAILED: "red",
  // KYC
  PENDING_REVIEW: "yellow",
  APPROVED: "green",
  // Dispute
  OPEN: "yellow",
  UNDER_REVIEW: "orange",
  RESOLVED: "green",
  // Match
  SUGGESTED: "blue",
  VIEWED: "gray",
  OFFER_SENT: "purple",
  // User
  ACTIVE: "green",
  DISABLED: "red",
  SUSPENDED: "orange",
  // Generic
  SUCCESS: "green",
  ERROR: "red",
};

const statusLabelMap: Record<string, string> = {
  PAYMENT_PENDING: "Payment Pending",
  PAYMENT_HELD: "Payment Held",
  IN_TRANSIT: "In Transit",
  DELIVERED_PENDING_VERIFICATION: "Pending Verification",
  PENDING_REVIEW: "Pending Review",
  OFFER_SENT: "Offer Sent",
  UNDER_REVIEW: "Under Review",
};

interface StatusBadgeProps {
  status: string;
  size?: "sm" | "md";
}

export function StatusBadge({ status, size = "md" }: StatusBadgeProps) {
  const variant: Variant = statusVariantMap[status] ?? "gray";
  const label = statusLabelMap[status] ?? status.replace(/_/g, " ");
  return <Badge variant={variant} size={size}>{label}</Badge>;
}
