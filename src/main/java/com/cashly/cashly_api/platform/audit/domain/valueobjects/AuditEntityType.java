package com.cashly.cashly_api.platform.audit.domain.valueobjects;

import java.util.Locale;

public enum AuditEntityType {
    QUOTE,
    ORDER,
    PURCHASE,
    INVOICE,
    INVOICE_LINE,
    RETURN,
    CREDIT_NOTE,
    DEBIT_NOTE,
    PAYMENT,
    PAYMENT_ALLOCATION,
    RECEIVABLE,
    CASH_REGISTER,
    CASH_MOVEMENT,
    PRODUCT,
    CATEGORY,
    PRICE_TYPE,
    WAREHOUSE,
    CUSTOMER,
    SUPPLIER,
    USER,
    ROLE,
    DEPARTMENT,
    COMPANY,
    PAYMENT_METHOD;

    public String labelKey() {
        return "audit.entities." + name();
    }

    public static AuditEntityType parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return AuditEntityType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
