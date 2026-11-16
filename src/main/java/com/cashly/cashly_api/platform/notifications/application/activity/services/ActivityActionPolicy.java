package com.cashly.cashly_api.platform.notifications.application.activity.services;

import java.util.Locale;
import java.util.Set;

public final class ActivityActionPolicy {

    private static final Set<String> MUTATING = Set.of("CREATE", "EDIT",
            "DELETE", "CONFIRM", "CANCEL", "DUPLICATE", "CONVERT_TO_ORDER",
            "CONVERT_TO_INVOICE", "OPEN", "CLOSE", "RECORD_ENTRY",
            "RECORD_EXIT", "BALANCE", "PAY_ALL", "PARTIAL_PAYMENT",
            "PAY_DIRECT", "PAY_WITH_VALIDATION", "APPROVE_PAYMENT",
            "REJECT_PAYMENT", "REFUND", "ADVANCE");

    private static final Set<String> READ_ONLY = Set.of("VIEW_LIST",
            "VIEW_DETAIL", "VIEW_STOCK", "MOVEMENT_HISTORY", "CASH_HISTORY",
            "VIEW_DEBT", "VIEW_BALANCE", "VIEW", "REPORT_PDF", "DOWNLOAD_PDF",
            "COMMERCIAL_CATALOG_PDF", "REPORT_STOCK", "REPORT_MOVEMENTS",
            "REPORT_CASH_REGISTER");

    private ActivityActionPolicy() {
    }

    public static boolean isMutating(String action) {
        return action != null && MUTATING.contains(action);
    }

    public static boolean isReadOnly(String action) {
        return action != null && READ_ONLY.contains(action);
    }

    public static Set<String> mutatingActions() {
        return MUTATING;
    }

    public static Set<String> readOnlyActions() {
        return READ_ONLY;
    }

    public static boolean isClassified(String action) {
        return MUTATING.contains(action) || READ_ONLY.contains(action);
    }

    public static String titleKey(String action) {
        return "notifications.activity." + key(action) + ".title";
    }

    public static String messageKey(String action) {
        return "notifications.activity." + key(action) + ".message";
    }

    private static String key(String action) {
        if (action == null) {
            return "unknown";
        }
        return switch (action) {
            case "PAY_ALL", "PARTIAL_PAYMENT", "PAY_DIRECT",
                    "PAY_WITH_VALIDATION" -> "pay";
            default -> action.toLowerCase(Locale.ROOT);
        };
    }
}
