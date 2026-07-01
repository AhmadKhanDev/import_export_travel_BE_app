import api from "@/lib/api";
import type {
  ApiResponse,
  Booking,
  BuyerRequest,
  CreateBuyerRequest,
  CreateOffer,
  CreateReview,
  CreateTravellerTrip,
  DeliveryCode,
  Match,
  Offer,
  PagedResponse,
  Payment,
  Review,
  TravellerTrip,
} from "@/types";

// Buyer Requests
export const buyerRequestsApi = {
  list: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<BuyerRequest>>>("/buyer-requests", { params }),
  my: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<BuyerRequest>>>("/buyer-requests/my", { params }),
  get: (id: string) => api.get<ApiResponse<BuyerRequest>>(`/buyer-requests/${id}`),
  create: (data: CreateBuyerRequest) =>
    api.post<ApiResponse<BuyerRequest>>("/buyer-requests", data),
  update: (id: string, data: Partial<CreateBuyerRequest>) =>
    api.put<ApiResponse<BuyerRequest>>(`/buyer-requests/${id}`, data),
  publish: (id: string) => api.post<ApiResponse<BuyerRequest>>(`/buyer-requests/${id}/publish`),
  cancel: (id: string) => api.post<ApiResponse<BuyerRequest>>(`/buyer-requests/${id}/cancel`),
  delete: (id: string) => api.delete<ApiResponse<void>>(`/buyer-requests/${id}`),
};

// Traveller Trips
export const travellerTripsApi = {
  list: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<TravellerTrip>>>("/traveller-trips", { params }),
  my: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<TravellerTrip>>>("/traveller-trips/my", { params }),
  get: (id: string) => api.get<ApiResponse<TravellerTrip>>(`/traveller-trips/${id}`),
  create: (data: CreateTravellerTrip) =>
    api.post<ApiResponse<TravellerTrip>>("/traveller-trips", data),
  update: (id: string, data: Partial<CreateTravellerTrip>) =>
    api.put<ApiResponse<TravellerTrip>>(`/traveller-trips/${id}`, data),
  publish: (id: string) => api.post<ApiResponse<TravellerTrip>>(`/traveller-trips/${id}/publish`),
  cancel: (id: string) => api.post<ApiResponse<TravellerTrip>>(`/traveller-trips/${id}/cancel`),
  delete: (id: string) => api.delete<ApiResponse<void>>(`/traveller-trips/${id}`),
};

// Matches
export const matchesApi = {
  generateByRequest: (buyerRequestId: string) =>
    api.post<ApiResponse<Match[]>>(`/matches/generate/by-request/${buyerRequestId}`),
  generateByTrip: (travellerTripId: string) =>
    api.post<ApiResponse<Match[]>>(`/matches/generate/by-trip/${travellerTripId}`),
  byRequest: (buyerRequestId: string, params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Match>>>(`/matches/by-request/${buyerRequestId}`, { params }),
  byTrip: (travellerTripId: string, params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Match>>>(`/matches/by-trip/${travellerTripId}`, { params }),
  get: (id: string) => api.get<ApiResponse<Match>>(`/matches/${id}`),
  reject: (id: string) => api.post<ApiResponse<Match>>(`/matches/${id}/reject`),
};

// Offers
export const offersApi = {
  create: (data: CreateOffer) => api.post<ApiResponse<Offer>>("/offers", data),
  get: (id: string) => api.get<ApiResponse<Offer>>(`/offers/${id}`),
  my: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Offer>>>("/offers/my", { params }),
  byRequest: (buyerRequestId: string, params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Offer>>>(`/offers/by-request/${buyerRequestId}`, { params }),
  byTrip: (travellerTripId: string, params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Offer>>>(`/offers/by-trip/${travellerTripId}`, { params }),
  accept: (id: string) => api.post<ApiResponse<Booking>>(`/offers/${id}/accept`),
  reject: (id: string) => api.post<ApiResponse<Offer>>(`/offers/${id}/reject`),
  cancel: (id: string) => api.post<ApiResponse<Offer>>(`/offers/${id}/cancel`),
};

// Bookings
export const bookingsApi = {
  my: (params?: Record<string, string>) =>
    api.get<ApiResponse<PagedResponse<Booking>>>("/bookings/my", { params }),
  get: (id: string) => api.get<ApiResponse<Booking>>(`/bookings/${id}`),
  cancel: (id: string) => api.post<ApiResponse<Booking>>(`/bookings/${id}/cancel`),
  markInTransit: (id: string) =>
    api.post<ApiResponse<Booking>>(`/bookings/${id}/mark-in-transit`),
  markDelivered: (id: string) =>
    api.post<ApiResponse<Booking>>(`/bookings/${id}/mark-delivered`),
};

// Payments
export const paymentsApi = {
  pay: (bookingId: string) =>
    api.post<ApiResponse<Payment>>(`/payments/${bookingId}/pay`, {}),
  getByBooking: (bookingId: string) =>
    api.get<ApiResponse<Payment>>(`/payments/${bookingId}`),
};

// Delivery
export const deliveryApi = {
  generate: (bookingId: string) =>
    api.post<ApiResponse<DeliveryCode>>(`/delivery-codes/${bookingId}/generate`),
  verify: (bookingId: string, code: string) =>
    api.post<ApiResponse<unknown>>(`/delivery-codes/${bookingId}/verify`, { code }),
  status: (bookingId: string) =>
    api.get<ApiResponse<{ status: string; expiresAt?: string }>>(`/delivery-codes/${bookingId}/status`),
};

// Reviews
export const reviewsApi = {
  create: (data: CreateReview) => api.post<ApiResponse<Review>>("/reviews", data),
  byBooking: (bookingId: string) =>
    api.get<ApiResponse<Review[]>>(`/reviews/booking/${bookingId}`),
};
