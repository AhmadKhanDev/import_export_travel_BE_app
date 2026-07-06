export interface TrackingSessionResponse {
  bookingId: string;
  active: boolean;
  shareable: boolean;
  hasLocation: boolean;
  latitude?: number;
  longitude?: number;
  accuracyMeters?: number;
  headingDegrees?: number;
  speedKph?: number;
  startedAt?: string;
  stoppedAt?: string;
  lastLocationAt?: string;
  stopReason?: string;
}

export interface TrackingLocationUpdateRequest {
  latitude: number;
  longitude: number;
  accuracyMeters?: number;
  headingDegrees?: number;
  speedKph?: number;
}
