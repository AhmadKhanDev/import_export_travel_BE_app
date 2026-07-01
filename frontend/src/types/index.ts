export type Role = "BUYER" | "TRAVELLER" | "ADMIN";
export type AccountStatus = "ACTIVE" | "SUSPENDED" | "DEACTIVATED";

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
  | "BOOKED"
  | "COMPLETED"
  | "CANCELLED";

export type OfferStatus = "SENT" | "ACCEPTED" | "REJECTED" | "EXPIRED" | "CANCELLED";

export type MatchStatus = "SUGGESTED" | "VIEWED" | "ACCEPTED" | "REJECTED" | "EXPIRED";

export type BookingStatus =
  | "PENDING_OFFER"
  | "OFFER_SENT"
  | "ACCEPTED"
  | "PAYMENT_PENDING"
  | "PAYMENT_HELD"
  | "IN_TRANSIT"
  | "DELIVERED_PENDING_VERIFICATION"
  | "DELIVERED"
  | "COMPLETED"
  | "CANCELLED"
  | "DISPUTED";

export type PaymentStatus =
  | "PENDING"
  | "HELD"
  | "RELEASED"
  | "REFUNDED"
  | "FAILED";

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface User {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: Role;
  accountStatus: AccountStatus;
  profileCompleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface BuyerRequest {
  id: string;
  buyerId: string;
  buyerName: string;
  title: string;
  description?: string;
  itemCategory: string;
  brand?: string;
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  estimatedItemPrice?: number;
  status: BuyerRequestStatus;
  neededBefore?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBuyerRequest {
  title: string;
  description?: string;
  itemCategory: string;
  brand?: string;
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  estimatedItemPrice?: number;
  neededBefore?: string;
}

export interface TravellerTrip {
  id: string;
  travellerId: string;
  travellerName: string;
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  travelDate: string;
  availableCapacityKg?: number;
  allowedItemTypes?: string;
  status: TravellerTripStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTravellerTrip {
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  travelDate: string;
  availableCapacityKg?: number;
  allowedItemTypes?: string;
}

export interface Match {
  id: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  buyerId: string;
  buyerName: string;
  travellerTripId: string;
  travellerId: string;
  travellerName: string;
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  itemCategory: string;
  neededBefore?: string;
  travelDate: string;
  matchScore: number;
  status: MatchStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Offer {
  id: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  travellerTripId: string;
  matchId?: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  itemPrice: number;
  travellerFee: number;
  platformFee: number;
  totalAmount: number;
  currency: string;
  message?: string;
  status: OfferStatus;
  expiresAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOffer {
  buyerRequestId: string;
  travellerTripId: string;
  matchId?: string;
  itemPrice: number;
  travellerFee: number;
  platformFee: number;
  currency?: string;
  message?: string;
  expiresAt?: string;
}

export interface Booking {
  id: string;
  offerId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  buyerRequestId: string;
  buyerRequestTitle: string;
  travellerTripId: string;
  sourceCountry: string;
  sourceCity: string;
  destinationCountry: string;
  destinationCity: string;
  itemPrice: number;
  travellerFee: number;
  platformFee: number;
  totalAmount: number;
  currency: string;
  status: BookingStatus;
  acceptedAt?: string;
  deliveredAt?: string;
  completedAt?: string;
  cancelledAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Payment {
  id: string;
  bookingId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  amount: number;
  platformFee: number;
  travellerPayout: number;
  currency: string;
  status: PaymentStatus;
  paidAt?: string;
  releasedAt?: string;
  createdAt: string;
}

export interface DeliveryCode {
  id: string;
  bookingId: string;
  code: string;
  status: string;
  expiresAt: string;
  createdAt: string;
  message?: string;
}

export interface Review {
  id: string;
  bookingId: string;
  reviewerId: string;
  reviewerName: string;
  revieweeId: string;
  revieweeName: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface CreateReview {
  bookingId: string;
  rating: number;
  comment?: string;
}
