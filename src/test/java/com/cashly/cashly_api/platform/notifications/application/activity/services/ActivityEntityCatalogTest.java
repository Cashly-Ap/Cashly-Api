package com.cashly.cashly_api.platform.notifications.application.activity.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityEntityCatalogTest {

    @Test
    void every_reference_type_has_a_noun_key() {
        for (String type : java.util.List.of("QUOTE", "ORDER", "PURCHASE",
                "PRODUCT", "CATEGORY", "PRICE_TYPE", "WAREHOUSE", "INVOICE",
                "RETURN", "CREDIT_NOTE", "DEBIT_NOTE", "CASH_REGISTER", "EXPENSE",
                "RECEIVABLE", "PAYMENT", "PAYMENT_METHOD", "CUSTOMER",
                "SUPPLIER", "USER", "ROLE", "DEPARTMENT", "COMPANY")) {
            assertThat(ActivityEntityCatalog.labelKey(type))
                    .startsWith("notifications.activity.entity.")
                    .endsWith(".noun");
            assertThat(ActivityEntityCatalog.displayLabelKey(type))
                    .startsWith("notifications.activity.entity.")
                    .endsWith(".label");
        }
    }

    @Test
    void unknown_reference_type_is_rejected() {
        assertThatThrownBy(() -> ActivityEntityCatalog.labelKey("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
