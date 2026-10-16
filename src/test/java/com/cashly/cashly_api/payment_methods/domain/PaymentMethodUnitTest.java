package com.cashly.cashly_api.payment_methods.domain;

import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMethod Domain Entity Unit Tests")
class PaymentMethodUnitTest {

    private static final String VALID_CODE = "PM-0001";
    private static final String VALID_NAME = "Cash";
    private static final String VALID_DESCRIPTION = "Cash payment method";

    

    @Test
    void should_CreateActivePaymentMethod_When_ValidData() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertNotNull(pm.getId());
        assertEquals(VALID_CODE, pm.getCode());
        assertEquals(VALID_NAME, pm.getName());
        assertEquals(VALID_DESCRIPTION, pm.getDescription());
        assertTrue(pm.isActive());
        assertNotNull(pm.getCreatedAt());
        assertNotNull(pm.getUpdatedAt());
    }

    @Test
    void should_CreateWithNullDescription_When_DescriptionIsNull() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, null);

        assertNull(pm.getDescription());
        assertTrue(pm.isActive());
    }

    @Test
    void should_ThrowException_When_CodeIsNull() {
        assertThrows(NullPointerException.class,
                () -> PaymentMethod.create(null, VALID_NAME, VALID_DESCRIPTION));
    }

    @Test
    void should_ThrowException_When_NameIsNull() {
        assertThrows(NullPointerException.class,
                () -> PaymentMethod.create(VALID_CODE, null, VALID_DESCRIPTION));
    }

    @Test
    void should_GenerateDifferentIds_When_CreateCalledTwice() {
        PaymentMethod pm1 = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        PaymentMethod pm2 = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertNotNull(pm1.getId());
        assertNotNull(pm2.getId());
        assertNotEquals(pm1.getId(), pm2.getId());
    }

    

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        PaymentMethodId id = PaymentMethodId.generate();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod pm = PaymentMethod.reconstitute(id, VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, true, now, now);

        assertEquals(id, pm.getId());
        assertEquals(VALID_CODE, pm.getCode());
        assertEquals(VALID_NAME, pm.getName());
        assertEquals(VALID_DESCRIPTION, pm.getDescription());
        assertTrue(pm.isActive());
        assertEquals(now, pm.getCreatedAt());
        assertEquals(now, pm.getUpdatedAt());
    }

    @Test
    void should_ReconstituteInactive_When_IsActiveFalse() {
        PaymentMethodId id = PaymentMethodId.generate();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod pm = PaymentMethod.reconstitute(id, VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, false, now, now);

        assertFalse(pm.isActive());
    }

    

    @Test
    void should_UpdateFields_When_ValidData() throws InterruptedException {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeUpdate = pm.getUpdatedAt();

        Thread.sleep(10);
        pm.update("Credit Card", "Credit card payment", true);

        assertEquals("Credit Card", pm.getName());
        assertEquals("Credit card payment", pm.getDescription());
        assertTrue(pm.isActive());
        assertTrue(pm.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_UpdateActive_When_IsActiveProvided() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        assertTrue(pm.isActive());

        pm.update(VALID_NAME, VALID_DESCRIPTION, false);

        assertFalse(pm.isActive());
    }

    @Test
    void should_NotChangeActive_When_IsActiveIsNull() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        pm.toggleActive();

        pm.update(VALID_NAME, VALID_DESCRIPTION, null);

        assertFalse(pm.isActive());
    }

    @Test
    void should_ThrowException_When_UpdateNameIsNull() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);

        assertThrows(NullPointerException.class,
                () -> pm.update(null, VALID_DESCRIPTION, null));
    }

    

    @Test
    void should_Deactivate_When_Active() throws InterruptedException {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        assertTrue(pm.isActive());
        LocalDateTime beforeToggle = pm.getUpdatedAt();

        Thread.sleep(10);
        pm.toggleActive();

        assertFalse(pm.isActive());
        assertTrue(pm.getUpdatedAt().isAfter(beforeToggle));
    }

    @Test
    void should_Activate_When_Inactive() throws InterruptedException {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        pm.toggleActive();
        assertFalse(pm.isActive());
        LocalDateTime beforeToggle = pm.getUpdatedAt();

        Thread.sleep(10);
        pm.toggleActive();

        assertTrue(pm.isActive());
        assertTrue(pm.getUpdatedAt().isAfter(beforeToggle));
    }

    

    @Test
    void should_Deactivate_When_Called() throws InterruptedException {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        assertTrue(pm.isActive());
        LocalDateTime beforeDeactivate = pm.getUpdatedAt();

        Thread.sleep(10);
        pm.deactivate();

        assertFalse(pm.isActive());
        assertTrue(pm.getUpdatedAt().isAfter(beforeDeactivate));
    }

    @Test
    void should_RemainInactive_When_DeactivateCalledOnInactive() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        pm.deactivate();
        pm.deactivate();

        assertFalse(pm.isActive());
    }

    

    @Test
    void should_BeEqual_When_SameId() {
        PaymentMethodId id = PaymentMethodId.generate();
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm1 = PaymentMethod.reconstitute(id, "PM-0001", "Cash", "desc", true, now, now);
        PaymentMethod pm2 = PaymentMethod.reconstitute(id, "PM-0002", "Credit Card", "other desc", false, now, now);

        assertEquals(pm1, pm2);
        assertEquals(pm1.hashCode(), pm2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm1 = PaymentMethod.reconstitute(PaymentMethodId.generate(), VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, true, now, now);
        PaymentMethod pm2 = PaymentMethod.reconstitute(PaymentMethodId.generate(), VALID_CODE, VALID_NAME,
                VALID_DESCRIPTION, true, now, now);

        assertNotEquals(pm1, pm2);
    }

    

    @Test
    void toString_Should_ContainFields() {
        PaymentMethod pm = PaymentMethod.create(VALID_CODE, VALID_NAME, VALID_DESCRIPTION);
        String str = pm.toString();

        assertTrue(str.contains(VALID_CODE));
        assertTrue(str.contains(VALID_NAME));
        assertTrue(str.contains("active=true"));
    }
}
