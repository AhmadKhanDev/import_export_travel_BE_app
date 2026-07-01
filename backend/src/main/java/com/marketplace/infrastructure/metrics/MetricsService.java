package com.marketplace.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetricsService {

    private final Counter registerCount;
    private final Counter loginSuccessCount;
    private final Counter loginFailureCount;
    private final Counter offersCreatedCount;
    private final Counter bookingsCreatedCount;
    private final Counter paymentsHeldCount;
    private final Counter paymentsReleasedCount;
    private final Counter deliveryVerifiedCount;
    private final Counter disputesCreatedCount;

    public MetricsService(MeterRegistry registry) {
        this.registerCount = Counter.builder("auth.register.count")
                .description("Number of successful user registrations")
                .register(registry);
        this.loginSuccessCount = Counter.builder("auth.login.success.count")
                .description("Number of successful logins")
                .register(registry);
        this.loginFailureCount = Counter.builder("auth.login.failure.count")
                .description("Number of failed login attempts")
                .register(registry);
        this.offersCreatedCount = Counter.builder("offers.created.count")
                .description("Number of offers created")
                .register(registry);
        this.bookingsCreatedCount = Counter.builder("bookings.created.count")
                .description("Number of bookings created")
                .register(registry);
        this.paymentsHeldCount = Counter.builder("payments.held.count")
                .description("Number of payments held in escrow")
                .register(registry);
        this.paymentsReleasedCount = Counter.builder("payments.released.count")
                .description("Number of payments released to traveller")
                .register(registry);
        this.deliveryVerifiedCount = Counter.builder("delivery.verified.count")
                .description("Number of successful delivery verifications")
                .register(registry);
        this.disputesCreatedCount = Counter.builder("disputes.created.count")
                .description("Number of disputes created")
                .register(registry);
    }

    public void incrementRegister() {
        registerCount.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccessCount.increment();
    }

    public void incrementLoginFailure() {
        loginFailureCount.increment();
    }

    public void incrementOffersCreated() {
        offersCreatedCount.increment();
    }

    public void incrementBookingsCreated() {
        bookingsCreatedCount.increment();
    }

    public void incrementPaymentsHeld() {
        paymentsHeldCount.increment();
    }

    public void incrementPaymentsReleased() {
        paymentsReleasedCount.increment();
    }

    public void incrementDeliveryVerified() {
        deliveryVerifiedCount.increment();
    }

    public void incrementDisputesCreated() {
        disputesCreatedCount.increment();
    }
}
