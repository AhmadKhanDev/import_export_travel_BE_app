export const ROLES = {
  BUYER: "BUYER",
  TRAVELLER: "TRAVELLER",
  ADMIN: "ADMIN",
} as const;

export const BOOKING_STATUSES = [
  "ACCEPTED",
  "PAYMENT_PENDING",
  "PAYMENT_HELD",
  "IN_TRANSIT",
  "DELIVERED_PENDING_VERIFICATION",
  "DELIVERED",
  "COMPLETED",
  "CANCELLED",
  "DISPUTED",
] as const;

export const DISPUTE_REASONS = [
  { value: "ITEM_NOT_DELIVERED", label: "Item not delivered" },
  { value: "ITEM_DAMAGED", label: "Item damaged" },
  { value: "WRONG_ITEM", label: "Wrong item delivered" },
  { value: "PAYMENT_ISSUE", label: "Payment issue" },
  { value: "OTHER", label: "Other" },
] as const;

export const ITEM_CATEGORIES = [
  "Electronics",
  "Clothing & Accessories",
  "Health & Beauty",
  "Books & Media",
  "Food & Beverages",
  "Toys & Games",
  "Sports & Outdoors",
  "Home & Kitchen",
  "Automotive",
  "Other",
];

export const CURRENCIES = ["USD", "GBP", "EUR", "CAD", "AUD", "AED"];
