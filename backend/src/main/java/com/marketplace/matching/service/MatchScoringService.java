package com.marketplace.matching.service;

import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.TravellerTrip;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

/**
 * Rule-based match scoring (0-100). Designed to be replaced or augmented by AI-based scoring later.
 */
@Service
public class MatchScoringService {

    private static final BigDecimal MINIMUM_SCORE = BigDecimal.valueOf(50);

    public boolean isEligible(BuyerRequest buyerRequest, TravellerTrip travellerTrip) {
        if (!equalsIgnoreCaseTrimmed(buyerRequest.getSourceCountry(), travellerTrip.getSourceCountry())) {
            return false;
        }
        if (!equalsIgnoreCaseTrimmed(buyerRequest.getDestinationCountry(), travellerTrip.getDestinationCountry())) {
            return false;
        }
        if (!isTravelDateAcceptable(buyerRequest.getNeededBefore(), travellerTrip.getTravelDate())) {
            return false;
        }
        return calculateScore(buyerRequest, travellerTrip).compareTo(MINIMUM_SCORE) >= 0;
    }

    public BigDecimal calculateScore(BuyerRequest buyerRequest, TravellerTrip travellerTrip) {
        int score = 0;

        if (equalsIgnoreCaseTrimmed(buyerRequest.getSourceCountry(), travellerTrip.getSourceCountry())) {
            score += 25;
        }

        if (equalsIgnoreCaseTrimmed(buyerRequest.getDestinationCountry(), travellerTrip.getDestinationCountry())) {
            score += 25;
        }

        if (equalsIgnoreCaseTrimmed(buyerRequest.getSourceCity(), travellerTrip.getSourceCity())) {
            score += 15;
        } else if (equalsIgnoreCaseTrimmed(buyerRequest.getSourceCountry(), travellerTrip.getSourceCountry())) {
            score += 5;
        }

        if (equalsIgnoreCaseTrimmed(buyerRequest.getDestinationCity(), travellerTrip.getDestinationCity())) {
            score += 15;
        } else if (equalsIgnoreCaseTrimmed(buyerRequest.getDestinationCountry(), travellerTrip.getDestinationCountry())) {
            score += 5;
        }

        Instant neededBefore = buyerRequest.getNeededBefore();
        Instant travelDate = travellerTrip.getTravelDate();
        if (neededBefore == null) {
            score += 5;
        } else if (travelDate != null && !travelDate.isAfter(neededBefore)) {
            score += 15;
        }

        score += scoreItemCategory(buyerRequest.getItemCategory(), travellerTrip.getAllowedItemTypes());

        return BigDecimal.valueOf(Math.min(score, 100));
    }

    public boolean isTravelDateAcceptable(Instant neededBefore, Instant travelDate) {
        if (neededBefore == null || travelDate == null) {
            return true;
        }
        return !travelDate.isAfter(neededBefore);
    }

    private int scoreItemCategory(String itemCategory, String allowedItemTypes) {
        if (isBlank(allowedItemTypes)) {
            return 2;
        }
        if (isBlank(itemCategory)) {
            return 0;
        }
        String normalizedCategory = itemCategory.trim().toLowerCase();
        return Arrays.stream(allowedItemTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(type -> type.equalsIgnoreCase(normalizedCategory)) ? 5 : 0;
    }

    private boolean equalsIgnoreCaseTrimmed(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}