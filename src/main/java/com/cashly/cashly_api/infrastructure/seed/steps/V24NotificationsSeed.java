package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityActionPolicy;
import com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityEntityCatalog;
import com.cashly.cashly_api.platform.notifications.infrastructure.persistence.entities.NotificationEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V24NotificationsSeed implements SeedStep {

    private static final String ACTIVITY_ACTOR = "operator";

    private final EntityManager entityManager;

    public V24NotificationsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 24;
    }

    @Override
    public void seed() {
        notification("one", ADMIN_USER, "PAYMENT_APPROVED", "INVOICE",
                "Payment received", "Invoice payment was recorded.",
                "notifications.payment_received.title",
                "notifications.payment_received.message", List.of(),
                INVOICE_ONE, false);
        notification("two", OPERATOR_USER, "QUOTE_CREATED", "QUOTE",
                "Quote created", "A customer quote is ready.",
                "notifications.quote_created.title",
                "notifications.quote_created.message", List.of(ACTIVITY_ACTOR),
                QUOTE_TWO, true);

        activity("category-food", CATEGORY_FOOD, "CATEGORY", "Food");
        activity("category-drinks", CATEGORY_DRINKS, "CATEGORY", "Drinks");
        activity("product-coffee", PRODUCT_COFFEE, "PRODUCT", "PRD-0001");
        activity("product-water", PRODUCT_WATER, "PRODUCT", "PRD-0002");
        activity("warehouse-main", WAREHOUSE_MAIN, "WAREHOUSE", "MAIN");
        activity("customer-one", CUSTOMER_ONE, "CUSTOMER",
                "Mercado Central, C.A.");
        activity("customer-two", CUSTOMER_TWO, "CUSTOMER", "Ana Pérez");
        activity("quote-one", QUOTE_ONE, "QUOTE", "PRE-2026-0001");
        activity("order-one", ORDER_ONE, "ORDER", "ORD-2026-0001");
        activity("invoice-one", INVOICE_ONE, "INVOICE", "INV-2026-0001");

        moduleNotifications();
    }

    private void moduleNotifications() {
        moduleActivity("catalog-category", "CREATE", "CATEGORY",
                CATEGORY_FOOD, "Food", true, at(0, 9));
        moduleActivity("catalog-product", "CREATE", "PRODUCT", PRODUCT_COFFEE,
                "PRD-0001 · Ground coffee", false, at(1, 10));
        moduleActivity("catalog-price-type", "CREATE", "PRICE_TYPE",
                id("price-type.base"), "Base", true, at(2, 11));
        moduleActivity("inventory-warehouse", "CREATE", "WAREHOUSE",
                WAREHOUSE_MAIN, "MAIN", true, at(3, 9));
        moduleActivity("sales-quote", "CREATE", "QUOTE", QUOTE_ONE,
                "PRE-2026-0001", false, at(4, 10));
        moduleActivity("sales-order", "CONFIRM", "ORDER", ORDER_ONE,
                "ORD-2026-0001", false, at(5, 11));
        moduleActivity("billing-invoice", "CONFIRM", "INVOICE", INVOICE_ONE,
                "INV-2026-0001", false, at(6, 12));
        moduleActivity("billing-credit-note", "CREATE", "CREDIT_NOTE",
                id("credit-note.one"), "CN-2026-0001", false, at(7, 9));
        moduleActivity("billing-return", "CREATE", "RETURN", RETURN_ONE,
                "RET-2026-0001", false, at(8, 10));
        moduleActivity("billing-debit-note", "CREATE", "DEBIT_NOTE",
                id("debit-note.one"), "DN-2026-0001", false, at(9, 11));
        moduleActivity("procurement-purchase", "CONFIRM", "PURCHASE",
                id("purchase.one"), "PUR-2026-0001", false, at(10, 12));
        moduleActivity("treasury-payment", "PAY_ALL", "PAYMENT", PAYMENT_ONE,
                "PAY-2026-0001", true, at(11, 9));
        moduleActivity("treasury-receivable", "PAY_ALL", "RECEIVABLE",
                CUSTOMER_ONE, "Mercado Central, C.A.", false, at(0, 10));
        moduleActivity("treasury-payment-method", "CREATE", "PAYMENT_METHOD",
                id("payment-method.card"), "Credit/Debit card", true,
                at(1, 11));
        moduleActivity("treasury-cash-register-open", "OPEN", "CASH_REGISTER",
                id("cash-register.open"), "Caja 2026-11-01", true, at(2, 12));
        moduleActivity("treasury-cash-register-close", "CLOSE",
                "CASH_REGISTER", id("cash-register.month-2026-10"),
                "Caja 2026-10-31", false, at(3, 13));
        moduleActivity("treasury-expense", "RECORD_EXIT", "EXPENSE",
                id("cash-movement.2026-01-31.purchase-" + id("purchase.cash-one")),
                "Compra al contado PUR-2026-0200", false, at(4, 14));
        moduleActivity("partners-customer", "CREATE", "CUSTOMER", CUSTOMER_ONE,
                "Mercado Central, C.A.", true, at(5, 9));
        moduleActivity("partners-supplier", "CREATE", "SUPPLIER",
                id("supplier.one"), "Coffee Growers, C.A.", true, at(6, 10));
        moduleActivity("platform-user", "CREATE", "USER", OPERATOR_USER,
                "operator", false, at(7, 11));
        moduleActivity("platform-role", "CREATE", "ROLE", OPERATOR_ROLE,
                "Operator", true, at(8, 12));
        moduleActivity("platform-department", "CREATE", "DEPARTMENT",
                SALES_DEPARTMENT, "Sales", true, at(9, 13));
        moduleActivity("platform-company", "EDIT", "COMPANY", COMPANY,
                "Cashly Demo, C.A.", true, at(10, 14));
    }

    private void moduleActivity(String key, String action, String referenceType,
            UUID referenceId, String referenceLabel, boolean read,
            LocalDateTime createdAt) {
        var id = id("notification.module-" + key);
        if (entityManager.find(NotificationEntity.class, id) != null) {
            return;
        }
        String titleKey = ActivityActionPolicy.titleKey(action);
        String messageKey = ActivityActionPolicy.messageKey(action);
        List<String> arguments = List.of(ACTIVITY_ACTOR,
                "i18n:" + ActivityEntityCatalog.labelKey(referenceType),
                referenceLabel,
                "i18n:" + ActivityEntityCatalog
                        .displayLabelKey(referenceType));
        var notification = new NotificationEntity();
        notification.setId(id);
        notification.setUserId(ADMIN_USER);
        notification.setType("ACTIVITY");
        notification.setTitle("New activity");
        notification.setMessage(String.join(" ", arguments));
        notification.setTitleKey(titleKey);
        notification.setMessageKey(messageKey);
        notification.setMessageArguments(arguments);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRead(read);
        notification.setCreatedAt(createdAt);
        if (read) {
            notification.setReadAt(createdAt.plusHours(1));
        }
        entityManager.persist(notification);
    }

    private static LocalDateTime at(int monthIndex, int hour) {
        return LocalDateTime.of(2025, 12, 15, 0, 0)
                .plusMonths(monthIndex % 12).withHour(hour);
    }

    private void activity(String key, UUID referenceId, String referenceType,
            String referenceLabel) {
        String entityKey = ActivityEntityCatalog.labelKey(referenceType);
        String titleKey = "notifications.activity.create.title";
        String messageKey = "notifications.activity.create.message";
        List<String> arguments = List.of(ACTIVITY_ACTOR, "i18n:" + entityKey,
                referenceLabel,
                "i18n:" + ActivityEntityCatalog
                        .displayLabelKey(referenceType));
        activityNotification(key, ADMIN_USER, titleKey, messageKey, arguments,
                referenceType, referenceId);
    }

    private void activityNotification(String key, UUID userId, String titleKey,
            String messageKey, List<String> arguments, String referenceType,
            UUID referenceId) {
        var id = id("notification." + key);
        if (entityManager.find(NotificationEntity.class, id) != null) {
            return;
        }
        var notification = new NotificationEntity();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setType("ACTIVITY");
        notification.setTitleKey(titleKey);
        notification.setMessageKey(messageKey);
        notification.setTitle("New activity");
        notification.setMessage(String.join(" ", arguments));
        notification.setMessageArguments(arguments);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.of(2026, 1, 16, 10, 30));
        entityManager.persist(notification);
    }

    private void notification(String key, UUID userId, String type,
            String referenceType, String title, String message,
            String titleKey, String messageKey, List<String> arguments,
            UUID referenceId, boolean read) {
        var id = id("notification." + key);
        if (entityManager.find(NotificationEntity.class, id) != null) {
            return;
        }
        var notification = new NotificationEntity();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTitleKey(titleKey);
        notification.setMessageKey(messageKey);
        notification.setMessageArguments(arguments);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.of(2026, 1, 15, 8, 0));
        if (read) {
            notification.setReadAt(LocalDateTime.of(2026, 1, 15, 9, 0));
        }
        entityManager.persist(notification);
    }
}
