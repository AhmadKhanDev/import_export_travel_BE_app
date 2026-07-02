export type DisputeStatus = "OPEN" | "UNDER_REVIEW" | "RESOLVED" | "REJECTED";
export type DisputeReason =
  | "ITEM_NOT_DELIVERED"
  | "ITEM_DAMAGED"
  | "WRONG_ITEM"
  | "PAYMENT_ISSUE"
  | "OTHER";

export interface DisputeResponse {
  id: string;
  bookingId: string;
  raisedById: string;
  raisedByName: string;
  reason: DisputeReason;
  description: string;
  status: DisputeStatus;
  resolution?: string;
  resolvedById?: string;
  resolvedByName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDisputeRequest {
  bookingId: string;
  reason: DisputeReason;
  description: string;
}

export interface AdminDisputeResponse extends DisputeResponse {
  buyerName: string;
  travellerName: string;
}

export interface ResolveDisputeRequest {
  resolution: string;
  refundBuyer?: boolean;
}

export interface RejectDisputeRequest {
  reason: string;
}
