package com.cashly.cashly_api.platform.notifications.application.activity.services;

import java.util.Map;

public final class ActivityEntityCatalog {

    private static final String PREFIX = "notifications.activity.entity.";
    private static final String NOUN_SUFFIX = ".noun";
    private static final String LABEL_SUFFIX = ".label";

    private static final Map<String, String> ENTITIES = Map.ofEntries(
            Map.entry("QUOTE", "quote"),
            Map.entry("ORDER", "order"),
            Map.entry("PURCHASE", "purchase"),
            Map.entry("PRODUCT", "product"),
            Map.entry("CATEGORY", "category"),
            Map.entry("PRICE_TYPE", "price_type"),
            Map.entry("WAREHOUSE", "warehouse"),
            Map.entry("INVOICE", "invoice"),
            Map.entry("RETURN", "return"),
            Map.entry("CREDIT_NOTE", "credit_note"),
            Map.entry("DEBIT_NOTE", "debit_note"),
            Map.entry("CASH_REGISTER", "cash_register"),
            Map.entry("EXPENSE", "expense"),
            Map.entry("RECEIVABLE", "receivable"),
            Map.entry("PAYMENT", "payment"),
            Map.entry("PAYMENT_METHOD", "payment_method"),
            Map.entry("CUSTOMER", "customer"),
            Map.entry("SUPPLIER", "supplier"),
            Map.entry("USER", "user"),
            Map.entry("ROLE", "role"),
            Map.entry("DEPARTMENT", "department"),
            Map.entry("COMPANY", "company"));

    private ActivityEntityCatalog() {
    }

    public static String labelKey(String referenceType) {
        return PREFIX + entityKey(referenceType) + NOUN_SUFFIX;
    }

    public static String displayLabelKey(String referenceType) {
        return PREFIX + entityKey(referenceType) + LABEL_SUFFIX;
    }

    private static String entityKey(String referenceType) {
        var key = ENTITIES.get(referenceType);
        if (key == null) {
            throw new IllegalArgumentException(
                    "Unknown activity reference type: " + referenceType);
        }
        return key;
    }

    public static boolean contains(String referenceType) {
        return ENTITIES.containsKey(referenceType);
    }
}
