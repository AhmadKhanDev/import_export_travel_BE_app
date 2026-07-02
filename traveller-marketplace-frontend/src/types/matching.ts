export type MatchStatus = "SUGGESTED" | "VIEWED" | "REJECTED" | "OFFER_SENT" | "BOOKED";

export interface MatchResponse {
  id: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  travellerTripId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  matchScore: number;
  status: MatchStatus;
  createdAt: string;
}
