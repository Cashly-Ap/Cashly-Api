package com.cashly.cashly_api.cashregister.domain.valueobjects;

public enum CashMovementType {
    PAYMENT,
    ADVANCE,
    REFUND,
    INVOICE,
    CREDIT_NOTE,
    DEBIT_NOTE,
    RETURN,
    MANUAL_INCOME,
    EXPENSE,
    WITHDRAWAL,
    ADJUSTMENT;

    public boolean affectsCashBalance() {
        return switch (this) {
            case PAYMENT, ADVANCE, REFUND, MANUAL_INCOME, EXPENSE, WITHDRAWAL, ADJUSTMENT -> true;
            case INVOICE, CREDIT_NOTE, DEBIT_NOTE, RETURN -> false;
        };
    }
}
