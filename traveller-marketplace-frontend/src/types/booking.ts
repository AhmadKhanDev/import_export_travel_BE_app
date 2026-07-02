export type BookingStatus =
  | "ACCEPTED"
  | "PAYMENT_PENDING"
  | "PAYMENT_HELD"
  | "IN_TRANSIT"
  | "DELIVERED_PENDING_VERIFICATION"
  | "DELIVERED"
  | "COMPLETED"
  | "CANCELLED"
  | "DISPUTED";

export interface BookingResponse {
  id: string;
  offerId: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  travellerTripId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  itemPrice: number;
  travellerFee: number;
  platformFee: number;
  totalAmount: number;
  currency: string;
  sourceCity: string;
  sourceCountry: string;
  destinationCity: string;
  destinationCountry: string;
  status: BookingStatus;
  acceptedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}
