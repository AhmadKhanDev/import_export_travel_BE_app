package com.marketplace.booking.service;

import com.marketplace.booking.dto.AdminBookingResponse;
import com.marketplace.booking.dto.BookingFilter;
import com.marketplace.booking.dto.BookingResponse;
import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.mapper.BookingMapper;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.booking.repository.BookingSpecification;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Set<BookingStatus> NON_CANCELLABLE_STATUSES = EnumSet.of(
            BookingStatus.COMPLETED,
            BookingStatus.DELIVERED,
            BookingStatus.DELIVERED_PENDING_VERIFICATION,
            BookingStatus.DISPUTED,
            BookingStatus.CANCELLED);

    private static final Set<BookingStatus> CANCELLABLE_BEFORE_PAYMENT = EnumSet.of(
            BookingStatus.PAYMENT_PENDING,
            BookingStatus.PENDING_OFFER,
            BookingStatus.OFFER_SENT,
            BookingStatus.ACCEPTED);

    private final BookingRepository bookingRepository;
    private final BuyerRequestRepository buyerRequestRepository;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public Page<BookingResponse> findMyBookings(UUID userId, BookingStatus status, Pageable pageable) {
        BookingFilter filter = BookingFilter.builder().status(status).build();
        Specification<Booking> spec = BookingSpecification.forParticipant(userId, filter);
        return bookingRepository.findAll(spec, pageable).map(bookingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId, UserPrincipal principal) {
        Booking booking = getBooking(bookingId);
        assertCanView(booking, principal);
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(UUID bookingId, UserPrincipal principal) {
        Booking booking = getBooking(bookingId);
        assertParticipant(booking, principal);

        if (NON_CANCELLABLE_STATUSES.contains(booking.getStatus())) {
            throw new InvalidStatusException("Booking cannot be cancelled in status " + booking.getStatus());
        }
        if (!CANCELLABLE_BEFORE_PAYMENT.contains(booking.getStatus())) {
            throw new BadRequestException(
                    "Booking can only be cancelled before payment is held. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking = bookingRepository.save(booking);

        revertBuyerRequestIfAppropriate(booking.getBuyerRequest());

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse markInTransit(UUID bookingId, UUID travellerId) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwns(booking, travellerId);

        if (booking.getStatus() != BookingStatus.PAYMENT_HELD) {
            throw new BadRequestException(
                    "Booking must be in PAYMENT_HELD status before marking in transit. "
                            + "Payment module will set this status after successful payment. Current status: "
                            + booking.getStatus());
        }

        booking.setStatus(BookingStatus.IN_TRANSIT);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse markDelivered(UUID bookingId, UUID travellerId) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwns(booking, travellerId);

        if (booking.getStatus() != BookingStatus.IN_TRANSIT) {
            throw new InvalidStatusException("Booking must be IN_TRANSIT before marking as delivered");
        }

        booking.setStatus(BookingStatus.DELIVERED_PENDING_VERIFICATION);
        booking.setDeliveredAt(Instant.now());
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> adminSearch(BookingFilter filter, Pageable pageable) {
        return bookingRepository.findAll(BookingSpecification.withFilter(filter), pageable)
                .map(bookingMapper::toAdminResponse);
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void assertCanView(Booking booking, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to this booking");
        }
    }

    private void assertParticipant(Booking booking, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("Only booking participants can cancel this booking");
        }
    }

    private void assertTravellerOwns(Booking booking, UUID travellerId) {
        if (!booking.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("Only the assigned traveller can perform this action");
        }
    }

    private void revertBuyerRequestIfAppropriate(BuyerRequest buyerRequest) {
        if (buyerRequest.getStatus() == BuyerRequestStatus.BOOKED) {
            buyerRequest.setStatus(BuyerRequestStatus.PUBLISHED);
            buyerRequestRepository.save(buyerRequest);
        }
    }
}