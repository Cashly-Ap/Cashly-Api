package com.parsun.parsun_api.quotes.domain.valueobjects;

public enum QuoteStatus {
    DRAFT,
    CONVERTED_TO_ORDER,
    CONVERTED_TO_INVOICE,
    CANCELLED;

    public boolean canTransitionTo(QuoteStatus target) {
        if (this == target) {
            return false;
        }
        return switch (this) {
            case DRAFT -> target == CONVERTED_TO_ORDER || target == CONVERTED_TO_INVOICE || target == CANCELLED;
            case CONVERTED_TO_ORDER, CONVERTED_TO_INVOICE, CANCELLED -> false;
        };
    }
}
