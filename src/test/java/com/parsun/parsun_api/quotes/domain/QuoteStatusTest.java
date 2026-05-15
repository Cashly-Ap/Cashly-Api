package com.parsun.parsun_api.quotes.domain;

import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuoteStatusTest {

    // Valid transitions

    @Test
    void should_allow_draft_to_converted_to_order() {
        assertTrue(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.CONVERTED_TO_ORDER));
    }

    @Test
    void should_allow_draft_to_converted_to_invoice() {
        assertTrue(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.CONVERTED_TO_INVOICE));
    }

    @Test
    void should_allow_draft_to_cancelled() {
        assertTrue(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.CANCELLED));
    }

    // Invalid transitions

    @Test
    void should_not_allow_draft_to_draft() {
        assertFalse(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.DRAFT));
    }

    @Test
    void should_not_allow_converted_to_order_to_any() {
        for (QuoteStatus target : QuoteStatus.values()) {
            assertFalse(QuoteStatus.CONVERTED_TO_ORDER.canTransitionTo(target),
                    "CONVERTED_TO_ORDER should not transition to " + target);
        }
    }

    @Test
    void should_not_allow_converted_to_invoice_to_any() {
        for (QuoteStatus target : QuoteStatus.values()) {
            assertFalse(QuoteStatus.CONVERTED_TO_INVOICE.canTransitionTo(target),
                    "CONVERTED_TO_INVOICE should not transition to " + target);
        }
    }

    @Test
    void should_not_allow_cancelled_to_any() {
        for (QuoteStatus target : QuoteStatus.values()) {
            assertFalse(QuoteStatus.CANCELLED.canTransitionTo(target),
                    "CANCELLED should not transition to " + target);
        }
    }

    // Self-transitions (staying in same state)

    @Test
    void should_not_allow_transition_to_same_state() {
        for (QuoteStatus status : QuoteStatus.values()) {
            assertFalse(status.canTransitionTo(status),
                    "Should not allow transition from " + status + " to itself");
        }
    }
}
