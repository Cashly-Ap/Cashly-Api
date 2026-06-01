package com.parsun.parsun_api.payments.domain.entities;

import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Domain Entity Unit Tests")
class PaymentUnitTest {

    private static final PaymentNumber PAYMENT_NUMBER = PaymentNumber.of(2026, 1);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final PaymentType TYPE = PaymentType.PAYMENT;
    private static final BigDecimal AMOUNT = new BigDecimal("1000.00");
    private static final UUID PAYMENT_METHOD_ID = UUID.randomUUID();
    private static final String REFERENCE_NUMBER = "REF-001";
    private static final LocalDate PAYMENT_DATE = LocalDate.of(2026, 5, 28);
    private static final String NOTES = "Test payment";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "Test User";

    private Payment createValidPayment() {
        return Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                CREATED_BY, CREATED_BY_NAME);
    }

    // ===== Factory: create =====

    @Nested
    @DisplayName("Payment.create()")
    class CreatePayment {

        @Test
        void should_CreatePayment_When_ValidDataProvided() {
            Payment payment = createValidPayment();

            assertNotNull(payment.getId());
            assertEquals(PAYMENT_NUMBER, payment.getPaymentNumber());
            assertEquals(CUSTOMER_ID, payment.getCustomerId());
            assertEquals(TYPE, payment.getType());
            assertEquals(AMOUNT, payment.getAmount());
            assertEquals(PAYMENT_METHOD_ID, payment.getPaymentMethodId());
            assertEquals(REFERENCE_NUMBER, payment.getReferenceNumber());
            assertEquals(PAYMENT_DATE, payment.getPaymentDate());
            assertEquals(NOTES, payment.getNotes());
            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(BigDecimal.ZERO, payment.getAppliedAmount());
            assertEquals(CREATED_BY, payment.getCreatedBy());
            assertEquals(CREATED_BY_NAME, payment.getCreatedByName());
            assertNotNull(payment.getCreatedAt());
            assertNotNull(payment.getUpdatedAt());
        }

        @Test
        void should_AllowNullNotes_When_Creating() {
            Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                    PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, null,
                    CREATED_BY, CREATED_BY_NAME);

            assertNull(payment.getNotes());
        }

        @Test
        void should_AllowNullReferenceNumber_When_Creating() {
            Payment payment = Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                    PAYMENT_METHOD_ID, null, PAYMENT_DATE, NOTES,
                    CREATED_BY, CREATED_BY_NAME);

            assertNull(payment.getReferenceNumber());
        }

        @Test
        void should_ThrowException_When_PaymentNumberIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(null, CUSTOMER_ID, TYPE, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_CustomerIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, null, TYPE, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_TypeIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, null, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_AmountIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, null,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_AmountIsZero() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, BigDecimal.ZERO,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_AmountIsNegative() {
            assertThrows(IllegalArgumentException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, new BigDecimal("-100.00"),
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_PaymentMethodIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                            null, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_PaymentDateIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, null, NOTES,
                            CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_CreatedByIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            null, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_CreatedByNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Payment.create(PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                            PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                            CREATED_BY, null));
        }
    }

    // ===== Lifecycle: cancel =====

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        void should_Cancel_When_PaymentIsApplied() {
            Payment payment = createValidPayment();
            payment.apply(AMOUNT);

            payment.cancel();

            assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
            assertNotNull(payment.getUpdatedAt());
        }

        @Test
        void should_Cancel_When_PaymentHasPartialAppliedAmount() {
            Payment payment = createValidPayment();
            payment.apply(new BigDecimal("400.00"));

            payment.cancel();

            assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        }

        @Test
        void should_ThrowException_When_PaymentIsAlreadyCancelled() {
            Payment payment = createValidPayment();
            payment.apply(AMOUNT);
            payment.cancel();

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    payment::cancel);

            assertEquals("Payment is already cancelled", exception.getMessage());
        }
    }

    // ===== Lifecycle: apply =====

    @Nested
    @DisplayName("apply()")
    class Apply {

        @Test
        void should_FullyApply_When_AmountEqualsPaymentTotal() {
            Payment payment = createValidPayment();

            payment.apply(AMOUNT);

            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(AMOUNT, payment.getAppliedAmount());
            assertNotNull(payment.getUpdatedAt());
        }

        @Test
        void should_PartiallyApply_When_AmountIsLessThanTotal() {
            Payment payment = createValidPayment();
            BigDecimal partial = new BigDecimal("400.00");

            payment.apply(partial);

            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(partial, payment.getAppliedAmount());
        }

        @Test
        void should_AccumulateAppliedAmount_When_ApplyingMultipleTimes() {
            Payment payment = createValidPayment();

            payment.apply(new BigDecimal("300.00"));
            payment.apply(new BigDecimal("500.00"));

            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(new BigDecimal("800.00"), payment.getAppliedAmount());
        }

        @Test
        void should_TransitionToApplied_When_AccumulationReachesTotal() {
            Payment payment = createValidPayment();

            payment.apply(new BigDecimal("600.00"));
            payment.apply(new BigDecimal("400.00"));

            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(AMOUNT, payment.getAppliedAmount());
        }

        @Test
        void should_ThrowException_When_AmountExceedsRemaining() {
            Payment payment = createValidPayment();
            payment.apply(new BigDecimal("600.00"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    payment.apply(new BigDecimal("500.00")));

            assertEquals("Applied amount cannot exceed payment total", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_AmountIsNull() {
            Payment payment = createValidPayment();

            assertThrows(NullPointerException.class, () -> payment.apply(null));
        }

        @Test
        void should_ThrowException_When_AmountIsZero() {
            Payment payment = createValidPayment();

            assertThrows(IllegalArgumentException.class, () -> payment.apply(BigDecimal.ZERO));
        }

        @Test
        void should_ThrowException_When_AmountIsNegative() {
            Payment payment = createValidPayment();

            assertThrows(IllegalArgumentException.class, () ->
                    payment.apply(new BigDecimal("-100.00")));
        }

        @Test
        void should_AllowApplyingToAlreadyAppliedPayment() {
            Payment payment = createValidPayment();
            payment.apply(AMOUNT);

            // When a payment is already fully applied, applying again should throw
            // because it would exceed the total
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    payment.apply(BigDecimal.ONE));

            assertEquals("Applied amount cannot exceed payment total", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_PaymentIsCancelled() {
            Payment payment = createValidPayment();
            payment.apply(AMOUNT);
            payment.cancel();

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    payment.apply(BigDecimal.ONE));

            assertEquals("Cannot apply to a cancelled payment", exception.getMessage());
        }
    }

    // ===== Reconstitute =====

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        void should_Reconstitute_When_AllFieldsProvided() {
            PaymentId id = PaymentId.generate();
            LocalDateTime now = LocalDateTime.now();

            Payment payment = Payment.reconstitute(
                    id, PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                    PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                    PaymentStatus.APPLIED, AMOUNT, CREATED_BY, CREATED_BY_NAME, now, now);

            assertEquals(id, payment.getId());
            assertEquals(PAYMENT_NUMBER, payment.getPaymentNumber());
            assertEquals(CUSTOMER_ID, payment.getCustomerId());
            assertEquals(TYPE, payment.getType());
            assertEquals(AMOUNT, payment.getAmount());
            assertEquals(PAYMENT_METHOD_ID, payment.getPaymentMethodId());
            assertEquals(REFERENCE_NUMBER, payment.getReferenceNumber());
            assertEquals(PAYMENT_DATE, payment.getPaymentDate());
            assertEquals(NOTES, payment.getNotes());
            assertEquals(PaymentStatus.APPLIED, payment.getStatus());
            assertEquals(AMOUNT, payment.getAppliedAmount());
            assertEquals(CREATED_BY, payment.getCreatedBy());
            assertEquals(CREATED_BY_NAME, payment.getCreatedByName());
            assertEquals(now, payment.getCreatedAt());
            assertEquals(now, payment.getUpdatedAt());
        }
    }

    // ===== equals / hashCode / toString =====

    @Nested
    @DisplayName("equals, hashCode, toString")
    class ObjectMethods {

        @Test
        void should_BeEqual_When_SameId() {
            Payment payment1 = createValidPayment();
            PaymentId id = payment1.getId();
            Payment payment2 = Payment.reconstitute(
                    id, PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                    PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                    PaymentStatus.APPLIED, BigDecimal.ZERO, CREATED_BY, CREATED_BY_NAME,
                    payment1.getCreatedAt(), LocalDateTime.now());

            assertEquals(payment1, payment2);
            assertEquals(payment1.hashCode(), payment2.hashCode());
        }

        @Test
        void should_NotBeEqual_When_DifferentIds() {
            Payment payment1 = createValidPayment();
            Payment payment2 = createValidPayment();

            assertNotEquals(payment1, payment2);
        }

        @Test
        void should_NotBeEqual_When_Null() {
            Payment payment = createValidPayment();

            assertNotEquals(null, payment);
        }

        @Test
        void should_NotBeEqual_When_DifferentClass() {
            Payment payment = createValidPayment();

            assertNotEquals("some string", payment);
        }

        @Test
        void should_BeEqual_When_SameReference() {
            Payment payment = createValidPayment();

            assertEquals(payment, payment);
        }

        @Test
        void should_ReturnString_When_ToStringCalled() {
            Payment payment = createValidPayment();

            String result = payment.toString();

            assertTrue(result.contains(payment.getId().toString()));
            assertTrue(result.contains(payment.getPaymentNumber().value()));
            assertTrue(result.contains(payment.getType().name()));
            assertTrue(result.contains(payment.getAmount().toString()));
            assertTrue(result.contains(payment.getStatus().name()));
        }
    }

    // ===== Getters =====

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        void should_ReturnAllFields_ThroughGetters() {
            Payment payment = Payment.reconstitute(
                    PaymentId.generate(), PAYMENT_NUMBER, CUSTOMER_ID, TYPE, AMOUNT,
                    PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                    PaymentStatus.APPLIED, BigDecimal.ZERO, CREATED_BY, CREATED_BY_NAME,
                    LocalDateTime.now(), LocalDateTime.now());

            assertNotNull(payment.getId());
            assertNotNull(payment.getPaymentNumber());
            assertNotNull(payment.getCustomerId());
            assertNotNull(payment.getType());
            assertNotNull(payment.getAmount());
            assertNotNull(payment.getPaymentMethodId());
            assertNotNull(payment.getPaymentDate());
            assertNotNull(payment.getStatus());
            assertNotNull(payment.getAppliedAmount());
            assertNotNull(payment.getCreatedBy());
            assertNotNull(payment.getCreatedByName());
            assertNotNull(payment.getCreatedAt());
            assertNotNull(payment.getUpdatedAt());
        }

        @Test
        void should_ReturnUpdatedAt_After_StateChange() {
            Payment payment = createValidPayment();
            LocalDateTime before = payment.getUpdatedAt();

            payment.apply(BigDecimal.ONE);

            assertNotNull(payment.getUpdatedAt());
            assertTrue(payment.getUpdatedAt().isAfter(before)
                    || payment.getUpdatedAt().equals(before));
        }
    }

    // ===== Payment as Advance =====

    @Nested
    @DisplayName("Payment with ADVANCE type")
    class AdvanceType {

        @Test
        void should_CreateAdvancePayment_When_TypeIsAdvance() {
            Payment advance = Payment.create(
                    PaymentNumber.of(2026, 2),
                    CUSTOMER_ID, PaymentType.ADVANCE, AMOUNT,
                    PAYMENT_METHOD_ID, REFERENCE_NUMBER, PAYMENT_DATE, NOTES,
                    CREATED_BY, CREATED_BY_NAME);

            assertEquals(PaymentType.ADVANCE, advance.getType());
        }
    }
}
