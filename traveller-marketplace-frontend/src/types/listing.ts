export type BuyerRequestStatus =
  | "DRAFT"
  | "PUBLISHED"
  | "MATCHED"
  | "BOOKED"
  | "COMPLETED"
  | "CANCELLED";

export type TravellerTripStatus =
  | "DRAFT"
  | "PUBLISHED"
  | "MATCHED"
  | "COMPLETED"
  | "CANCELLED";

export interface BuyerRequestResponse {
  id: string;
  buyerId: string;
  buyerName: string;
  title: string;
  description?: string;
  itemCategory: string;
  estimatedItemPrice?: number;
  currency: string;
  travellersReward: number;
  sourceCity: string;
  sourceCountry: string;
  destinationCity: string;
  destinationCountry: string;
  deadlineDate?: string;
  status: BuyerRequestStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBuyerRequestRequest {
  title: string;
  description?: string;
  itemCategory: string;
  estimatedItemPrice?: number;
  currency: string;
  travellersReward: number;
  sourceCity: string;
  sourceCountry: string;
  destinationCity: string;
  destinationCountry: string;
  deadlineDate?: string;
}

export interface UpdateBuyerRequestRequest extends Partial<CreateBuyerRequestRequest> {}

export interface TravellerTripResponse {
  id: string;
  travellerId: string;
  travellerName: string;
  sourceCity: string;
  sourceCountry: string;
  destinationCity: string;
  destinationCountry: string;
  travelDate: string;
  returnDate?: string;
  availableCapacityKg?: number;
  notes?: string;
  status: TravellerTripStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTravellerTripRequest {
  sourceCity: string;
  sourceCountry: string;
  destinationCity: string;
  destinationCountry: string;
  travelDate: string;
  returnDate?: string;
  availableCapacityKg?: number;
  notes?: string;
}

export interface UpdateTravellerTripRequest extends Partial<CreateTravellerTripRequest> {}
