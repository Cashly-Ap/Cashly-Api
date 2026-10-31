package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.notifications.infrastructure.persistence.entities.NotificationEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class V24NotificationsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V24NotificationsSeed(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public int version() { return 24; }
    @Override public void seed() {
        notification("one", ADMIN_USER, CUSTOMER_ONE, "PAYMENT", "Payment received", "Invoice payment was recorded.", INVOICE_ONE, false);
        notification("two", OPERATOR_USER, CUSTOMER_TWO, "QUOTE", "Quote created", "A customer quote is ready.", QUOTE_TWO, true);
    }
    private void notification(String key, java.util.UUID userId, java.util.UUID customerId, String type, String title,
                              String message, java.util.UUID referenceId, boolean read) {
        var id = id("notification." + key); if (entityManager.find(NotificationEntity.class, id) != null) return;
        var notification = new NotificationEntity(); notification.setId(id); notification.setUserId(userId); notification.setCustomerId(customerId);
        notification.setType(type); notification.setTitle(title); notification.setMessage(message); notification.setMessageArguments(java.util.List.of());
        notification.setReferenceType(type); notification.setReferenceId(referenceId); notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.of(2025, 1, 15, 8, 0)); if (read) notification.setReadAt(LocalDateTime.of(2025, 1, 15, 9, 0));
        entityManager.persist(notification);
    }
}
