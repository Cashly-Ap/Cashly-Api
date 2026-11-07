package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
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
        notification("one", ADMIN_USER, "PAYMENT",
                "Payment received", "Invoice payment was recorded.",
                INVOICE_ONE, false);
        notification("two", OPERATOR_USER, "QUOTE", "Quote created",
                "A customer quote is ready.", QUOTE_TWO, true);

        activity("category-food", CATEGORY_FOOD, "CATEGORY", "Food");
        activity("category-drinks", CATEGORY_DRINKS, "CATEGORY", "Drinks");
        activity("product-coffee", PRODUCT_COFFEE, "PRODUCT", "PRD-0001");
        activity("product-water", PRODUCT_WATER, "PRODUCT", "PRD-0002");
        activity("warehouse-main", WAREHOUSE_MAIN, "WAREHOUSE", "MAIN");
        activity("customer-one", CUSTOMER_ONE, "CUSTOMER",
                "Mercado Central, C.A.");
        activity("customer-two", CUSTOMER_TWO, "CUSTOMER", "Ana Pérez");
        activity("quote-one", QUOTE_ONE, "QUOTE", "PRE-2025-0001");
        activity("order-one", ORDER_ONE, "ORDER", "ORD-2025-0001");
        activity("invoice-one", INVOICE_ONE, "INVOICE", "INV-2025-0001");
    }

    private void activity(String key, UUID referenceId, String referenceType,
            String referenceLabel) {
        String entityKey = switch (referenceType) {
            case "CATEGORY" -> "notifications.activity.entity.category.noun";
            case "PRODUCT" -> "notifications.activity.entity.product.noun";
            case "WAREHOUSE" -> "notifications.activity.entity.warehouse.noun";
            case "CUSTOMER" -> "notifications.activity.entity.customer.noun";
            case "QUOTE" -> "notifications.activity.entity.quote.noun";
            case "ORDER" -> "notifications.activity.entity.order.noun";
            case "INVOICE" -> "notifications.activity.entity.invoice.noun";
            default -> throw new IllegalArgumentException(
                    "Unknown activity reference type: " + referenceType);
        };
        String titleKey = "notifications.activity.create.title";
        String messageKey = "notifications.activity.create.message";
        List<String> arguments = List.of(ACTIVITY_ACTOR, "i18n:" + entityKey,
                referenceLabel);
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
        notification.setCreatedAt(LocalDateTime.of(2025, 1, 16, 10, 30));
        entityManager.persist(notification);
    }

    private void notification(String key, UUID userId, String type,
            String title, String message, UUID referenceId, boolean read) {
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
        notification.setMessageArguments(List.of());
        notification.setReferenceType(type);
        notification.setReferenceId(referenceId);
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.of(2025, 1, 15, 8, 0));
        if (read) {
            notification.setReadAt(LocalDateTime.of(2025, 1, 15, 9, 0));
        }
        entityManager.persist(notification);
    }
}
