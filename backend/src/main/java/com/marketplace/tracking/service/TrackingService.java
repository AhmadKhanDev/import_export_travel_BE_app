package com.marketplace.tracking.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.realtime.service.RealtimeEventPublisher;
import com.marketplace.tracking.dto.TrackingLocationUpdateRequest;
import com.marketplace.tracking.dto.TrackingSessionResponse;
import com.marketplace.tracking.entity.BookingTrackingSession;
import com.marketplace.tracking.repository.BookingTrackingSessionRepository;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private static final Set<BookingStatus> TRACKING_ALLOWED_STATUSES = EnumSet.of(
            BookingStatus.PAYMENT_HELD,
            BookingStatus.IN_TRANSIT,
            BookingStatus.DELIVERED_PENDING_VERIFICATION
    );

    private final BookingRepository bookingRepository;
    private final BookingTrackingSessionRepository trackingSessionRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Transactional(readOnly = true)
    public TrackingSessionResponse getTrackingState(UUID bookingId, UserPrincipal principal) {
        Booking booking = getBooking(bookingId);
        assertCanView(booking, principal);
        BookingTrackingSession session = trackingSessionRepository.findByBooking_Id(bookingId).orElse(null);
        return toResponse(booking, session);
    }

    @Transactional
    public TrackingSessionResponse startTracking(UUID bookingId, UUID travellerId) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwnsBooking(booking, travellerId);
        assertTrackingAllowed(booking);

        BookingTrackingSession session = trackingSessionRepository.findByBooking_Id(bookingId)
                .orElseGet(() -> BookingTrackingSession.builder()
                        .id(UUID.randomUUID())
                        .booking(booking)
                        .traveller(booking.getTraveller())
                        .build());

        Instant now = Instant.now();
        session.setActive(true);
        session.setStartedAt(now);
        session.setStoppedAt(null);
        session.setStopReason(null);
        session = trackingSessionRepository.save(session);

        TrackingSessionResponse response = toResponse(booking, session);
        realtimeEventPublisher.publishTrackingUpdate(bookingId, response);
        log.info("Live tracking started: bookingId={}, travellerId={}", bookingId, travellerId);
        return response;
    }

    @Transactional
    public TrackingSessionResponse stopTracking(UUID bookingId, UUID travellerId) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwnsBooking(booking, travellerId);

        BookingTrackingSession session = trackingSessionRepository.findByBooking_Id(bookingId).orElse(null);
        if (session == null) {
            return toResponse(booking, null);
        }

        deactivateSession(session, "STOPPED_BY_TRAVELLER");
        TrackingSessionResponse response = toResponse(booking, session);
        realtimeEventPublisher.publishTrackingUpdate(bookingId, response);
        log.info("Live tracking stopped: bookingId={}, travellerId={}", bookingId, travellerId);
        return response;
    }

    @Transactional
    public TrackingSessionResponse updateLocation(UUID bookingId, UUID travellerId, TrackingLocationUpdateRequest request) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwnsBooking(booking, travellerId);
        assertTrackingAllowed(booking);

        BookingTrackingSession session = trackingSessionRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new BadRequestException("Tracking session has not been started"));

        if (!session.isActive()) {
            throw new BadRequestException("Tracking session is not active");
        }

        session.setLatitude(request.getLatitude());
        session.setLongitude(request.getLongitude());
        session.setAccuracyMeters(request.getAccuracyMeters());
        session.setHeadingDegrees(request.getHeadingDegrees());
        session.setSpeedKph(request.getSpeedKph());
        session.setLastLocationAt(Instant.now());
        session = trackingSessionRepository.save(session);

        TrackingSessionResponse response = toResponse(booking, session);
        realtimeEventPublisher.publishTrackingUpdate(bookingId, response);
        return response;
    }

    @Transactional
    public void stopTrackingSilently(UUID bookingId, String reason) {
        trackingSessionRepository.findByBooking_Id(bookingId)
                .filter(BookingTrackingSession::isActive)
                .ifPresent(session -> {
                    deactivateSession(session, reason);
                    realtimeEventPublisher.publishTrackingUpdate(bookingId, toResponse(session.getBooking(), session));
                    log.info("Live tracking auto-stopped: bookingId={}, reason={}", bookingId, reason);
                });
    }

    private void deactivateSession(BookingTrackingSession session, String reason) {
        session.setActive(false);
        session.setStoppedAt(Instant.now());
        session.setStopReason(reason);
        trackingSessionRepository.save(session);
    }

    private TrackingSessionResponse toResponse(Booking booking, BookingTrackingSession session) {
        boolean shareable = TRACKING_ALLOWED_STATUSES.contains(booking.getStatus());
        boolean hasLocation = session != null && session.getLatitude() != null && session.getLongitude() != null;

        return TrackingSessionResponse.builder()
                .bookingId(booking.getId())
                .active(session != null && session.isActive())
                .shareable(shareable)
                .hasLocation(hasLocation)
                .latitude(session != null ? session.getLatitude() : null)
                .longitude(session != null ? session.getLongitude() : null)
                .accuracyMeters(session != null ? session.getAccuracyMeters() : null)
                .headingDegrees(session != null ? session.getHeadingDegrees() : null)
                .speedKph(session != null ? session.getSpeedKph() : null)
                .startedAt(session != null ? session.getStartedAt() : null)
                .stoppedAt(session != null ? session.getStoppedAt() : null)
                .lastLocationAt(session != null ? session.getLastLocationAt() : null)
                .stopReason(session != null ? session.getStopReason() : null)
                .build();
    }

    private void assertTrackingAllowed(Booking booking) {
        if (!TRACKING_ALLOWED_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Live tracking is not available for booking status " + booking.getStatus());
        }
    }

    private void assertTravellerOwnsBooking(Booking booking, UUID travellerId) {
        if (!booking.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("Only the assigned traveller can share live location");
        }
    }

    private void assertCanView(Booking booking, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to live tracking for this booking");
        }
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }
}
