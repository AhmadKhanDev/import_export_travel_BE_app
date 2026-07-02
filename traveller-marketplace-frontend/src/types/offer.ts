export type OfferStatus = "SENT" | "ACCEPTED" | "REJECTED" | "CANCELLED" | "EXPIRED";

export interface OfferResponse {
  id: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  travellerTripId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  travellerFee: number;
  itemPrice: number;
  platformFee: number;
  totalAmount: number;
  currency: string;
  message?: string;
  status: OfferStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOfferRequest {
  buyerRequestId: string;
  travellerTripId: string;
  travellerFee: number;
  message?: string;
}
