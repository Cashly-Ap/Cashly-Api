package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Domain Entity Unit Tests")
class PaymentUnitTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID METHOD_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_NAME = "Test User";
    private static final PaymentNumber PAYMENT_NUMBER = PaymentNumber.of(2025, 1);

    // --- create() ---

    @Test
    void should_CreatePayment_With_DraftStatus() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, "REF-001", LocalDate.now(), "Notes", USER_ID, USER_NAME);

        assertNotNull(payment.getId());
        assertEquals(PAYMENT_NUMBER, payment.getPaymentNumber());
        assertEquals(PaymentStatus.DRAFT, payment.getStatus());
        assertEquals(BigDecimal.ZERO, payment.getAppliedAmount());
        assertEquals("REF-001", payment.getReferenceNumber());
        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void should_CreateAdvance_With_DraftStatus() {
        Payment advance = Payment.create(PaymentNumber.of(2025, 2), CUSTOMER_ID, PaymentType.ADVANCE,
                new BigDecimal("2000.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        assertEquals(PaymentType.ADVANCE, advance.getType());
        assertEquals(PaymentStatus.DRAFT, advance.getStatus());
        assertNull(advance.getReferenceNumber());
        assertNull(advance.getNotes());
    }

    @Test
    void should_ThrowException_When_AmountIsZero() {
        assertThrows(IllegalArgumentException.class, () -> Payment.create(PAYMENT_NUMBER, CUSTOMER_ID,
                PaymentType.PAYMENT, BigDecimal.ZERO, METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME));
    }

    @Test
    void should_ThrowException_When_AmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Payment.create(PAYMENT_NUMBER, CUSTOMER_ID,
                PaymentType.PAYMENT, new BigDecimal("-100"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME));
    }

    // --- reconstitute() ---

    @Test
    void should_Reconstitute_With_FullState() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.reconstitute(
                PaymentId.of(id), PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, "REF-001", LocalDate.now(), "Notes",
                PaymentStatus.APPLIED, new BigDecimal("500.00"), USER_ID, USER_NAME, now, now);

        assertEquals(id, payment.getId().value());
        assertEquals(PaymentStatus.APPLIED, payment.getStatus());
        assertEquals(new BigDecimal("500.00"), payment.getAppliedAmount());
    }

    // --- apply() ---

    @Test
    void should_ApplyFullAmount_When_AmountEqualsTotal() throws InterruptedException {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        LocalDateTime before = payment.getUpdatedAt();

        Thread.sleep(10);
        payment.apply(new BigDecimal("500.00"));

        assertEquals(PaymentStatus.APPLIED, payment.getStatus());
        assertEquals(new BigDecimal("500.00"), payment.getAppliedAmount());
        assertTrue(payment.getUpdatedAt().isAfter(before));
    }

    @Test
    void should_ApplyPartialAmount_When_AmountLessThanTotal() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        payment.apply(new BigDecimal("200.00"));

        assertEquals(PaymentStatus.PARTIALLY_APPLIED, payment.getStatus());
        assertEquals(new BigDecimal("200.00"), payment.getAppliedAmount());
    }

    @Test
    void should_ApplyRemainingAmount_When_PartiallyApplied() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        payment.apply(new BigDecimal("200.00"));
        payment.apply(new BigDecimal("300.00"));

        assertEquals(PaymentStatus.APPLIED, payment.getStatus());
        assertEquals(new BigDecimal("500.00"), payment.getAppliedAmount());
    }

    @Test
    void should_ThrowException_When_ApplyingExcessAmount() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        assertThrows(IllegalArgumentException.class, () -> payment.apply(new BigDecimal("600.00")));
    }

    @Test
    void should_ThrowException_When_ApplyingCancelledPayment() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        payment.apply(new BigDecimal("100.00"));
        payment.cancel();

        assertThrows(IllegalStateException.class, () -> payment.apply(new BigDecimal("100.00")));
    }

    // --- cancel() ---

    @Test
    void should_CancelAppliedPayment() throws InterruptedException {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        payment.apply(new BigDecimal("500.00"));
        LocalDateTime before = payment.getUpdatedAt();

        Thread.sleep(10);
        payment.cancel();

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertTrue(payment.getUpdatedAt().isAfter(before));
    }

    @Test
    void should_ThrowException_When_CancellingDraftPayment() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        assertThrows(IllegalStateException.class, () -> payment.cancel());
    }

    @Test
    void should_ThrowException_When_CancellingAlreadyCancelled() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        payment.apply(new BigDecimal("100.00"));
        payment.cancel();

        assertThrows(IllegalStateException.class, () -> payment.cancel());
    }

    // --- unapply() ---

    @Test
    void should_UnapplyAppliedPayment() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        payment.apply(new BigDecimal("500.00"));

        payment.unapply();

        assertEquals(PaymentStatus.DRAFT, payment.getStatus());
        assertEquals(BigDecimal.ZERO, payment.getAppliedAmount());
    }

    @Test
    void should_UnapplyPartiallyAppliedPayment() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);
        payment.apply(new BigDecimal("200.00"));

        payment.unapply();

        assertEquals(PaymentStatus.DRAFT, payment.getStatus());
        assertEquals(BigDecimal.ZERO, payment.getAppliedAmount());
    }

    @Test
    void should_ThrowException_When_UnapplyingDraftPayment() {
        Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, PaymentType.PAYMENT,
                new BigDecimal("500.00"), METHOD_ID, null, LocalDate.now(), null, USER_ID, USER_NAME);

        assertThrows(IllegalStateException.class, () -> payment.unapply());
    }

    // --- equals/hashCode ---

    @Test
    void should_BeEqual_When_SameId() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Payment p1 = Payment.reconstitute(PaymentId.of(id), PaymentNumber.of(2025, 1), CUSTOMER_ID,
                PaymentType.PAYMENT, new BigDecimal("500"), METHOD_ID, null, LocalDate.now(), null,
                PaymentStatus.DRAFT, BigDecimal.ZERO, USER_ID, USER_NAME, now, now);
        Payment p2 = Payment.reconstitute(PaymentId.of(id), PaymentNumber.of(2025, 2), CUSTOMER_ID,
                PaymentType.ADVANCE, new BigDecimal("1000"), METHOD_ID, null, LocalDate.now(), null,
                PaymentStatus.APPLIED, new BigDecimal("1000"), USER_ID, USER_NAME, now, now);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();
        Payment p1 = Payment.reconstitute(PaymentId.of(UUID.randomUUID()), PAYMENT_NUMBER, CUSTOMER_ID,
                PaymentType.PAYMENT, new BigDecimal("500"), METHOD_ID, null, LocalDate.now(), null,
                PaymentStatus.DRAFT, BigDecimal.ZERO, USER_ID, USER_NAME, now, now);
        Payment p2 = Payment.reconstitute(PaymentId.of(UUID.randomUUID()), PAYMENT_NUMBER, CUSTOMER_ID,
                PaymentType.PAYMENT, new BigDecimal("500"), METHOD_ID, null, LocalDate.now(), null,
                PaymentStatus.DRAFT, BigDecimal.ZERO, USER_ID, USER_NAME, now, now);

        assertNotEquals(p1, p2);
    }
}
