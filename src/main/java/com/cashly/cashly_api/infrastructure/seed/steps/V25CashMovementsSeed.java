package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashMovementJpaEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class V25CashMovementsSeed implements SeedStep {
    private final EntityManager entityManager;
    public V25CashMovementsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 25;
    }
    @Override
    public void seed() {
        movement("one", "IN", "PAYMENT", "10.00", PAYMENT_ONE,
                "Customer payment");
        movement("two", "OUT", "EXPENSE", "2.00", null, "Petty cash expense");
    }
    private void movement(String key, String direction, String type,
            String amount, java.util.UUID sourceId, String description) {
        var id = id("cash-movement." + key);
        if (entityManager.find(CashMovementJpaEntity.class, id) != null)
            return;
        var movement = new CashMovementJpaEntity();
        movement.setId(id);
        movement.setRegisterId(id("cash-register.closed"));
        movement.setDirection(direction);
        movement.setType(type);
        movement.setAmount(new BigDecimal(amount));
        movement.setPaymentMethodId(id("payment-method.cash"));
        movement.setPaymentMethodName("Cash");
        movement.setDescription(description);
        movement.setSourceType(sourceId == null ? "MANUAL" : "PAYMENT");
        movement.setSourceId(sourceId);
        movement.setCreatedBy(ADMIN_USER);
        movement.setCreatedByName("Cashly Administrator");
        movement.setOccurredAt(
                LocalDateTime.of(2025, 1, 10, key.equals("one") ? 10 : 11, 0));
        entityManager.persist(movement);
    }
}
