package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMethod Domain Entity Unit Tests")
class PaymentMethodUnitTest {

    private static final String VALID_CODE = "PM-0001";
    private static final String VALID_NAME = "Efectivo";
    private static final String VALID_DESCRIPTION = "Pago en efectivo";

    // --- create() ---

    @Test
    void should_CreateActivePaymentMethod_With_GeneratedId() {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertNotNull(method.getId());
        assertEquals(VALID_CODE, method.getCode());
        assertEquals(VALID_NAME, method.getName());
        assertEquals(VALID_DESCRIPTION, method.getDescription());
        assertTrue(method.isActive());
        assertNotNull(method.getCreatedAt());
        assertNotNull(method.getUpdatedAt());
    }

    @Test
    void should_CreatePaymentMethod_With_NullDescription() {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, null);

        assertNull(method.getDescription());
        assertTrue(method.isActive());
    }

    // --- reconstitute() ---

    @Test
    void should_Reconstitute_With_FullState() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod method = PaymentMethod.reconstitute(
                PaymentMethodId.of(id), "PM-0002", "Transferencia", "Bank transfer", false, now, now);

        assertEquals(id, method.getId().value());
        assertEquals("PM-0002", method.getCode());
        assertEquals("Transferencia", method.getName());
        assertEquals("Bank transfer", method.getDescription());
        assertFalse(method.isActive());
        assertEquals(now, method.getCreatedAt());
        assertEquals(now, method.getUpdatedAt());
    }

    // --- update() ---

    @Test
    void should_UpdateNameAndDescription_When_ValidData() throws InterruptedException {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeUpdate = method.getUpdatedAt();

        Thread.sleep(10);
        method.update("Updated Name", "Updated Description");

        assertEquals("Updated Name", method.getName());
        assertEquals("Updated Description", method.getDescription());
        assertTrue(method.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_ThrowException_When_NameIsNull() {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertThrows(NullPointerException.class, () -> method.update(null, "Description"));
    }

    // --- toggleActive() ---

    @Test
    void should_ToggleFromActiveToInactive() throws InterruptedException {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeToggle = method.getUpdatedAt();

        Thread.sleep(10);
        method.toggleActive();

        assertFalse(method.isActive());
        assertTrue(method.getUpdatedAt().isAfter(beforeToggle));
    }

    @Test
    void should_ToggleFromInactiveToActive() throws InterruptedException {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        method.deactivate();
        LocalDateTime beforeToggle = method.getUpdatedAt();

        Thread.sleep(10);
        method.toggleActive();

        assertTrue(method.isActive());
        assertTrue(method.getUpdatedAt().isAfter(beforeToggle));
    }

    // --- deactivate() ---

    @Test
    void should_Deactivate_When_Active() throws InterruptedException {
        PaymentMethod method = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeDeactivate = method.getUpdatedAt();

        Thread.sleep(10);
        method.deactivate();

        assertFalse(method.isActive());
        assertTrue(method.getUpdatedAt().isAfter(beforeDeactivate));
    }

    // --- equals/hashCode ---

    @Test
    void should_BeEqual_When_SameId() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod m1 = PaymentMethod.reconstitute(PaymentMethodId.of(id), "PM-0001", "Name 1", "Desc 1", true, now, now);
        PaymentMethod m2 = PaymentMethod.reconstitute(PaymentMethodId.of(id), "PM-0001", "Name 2", "Desc 2", false, now, now);

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod m1 = PaymentMethod.reconstitute(PaymentMethodId.of(UUID.randomUUID()), VALID_CODE, VALID_NAME, VALID_DESCRIPTION, true, now, now);
        PaymentMethod m2 = PaymentMethod.reconstitute(PaymentMethodId.of(UUID.randomUUID()), VALID_CODE, VALID_NAME, VALID_DESCRIPTION, true, now, now);

        assertNotEquals(m1, m2);
    }

    @Test
    void should_NotBeEqual_When_BothGeneratedIds() {
        // PaymentMethod.create() always generates a new ID, so two created instances
        // will have different IDs and therefore not be equal
        PaymentMethod m1 = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        PaymentMethod m2 = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertNotEquals(m1, m2);
    }
}
