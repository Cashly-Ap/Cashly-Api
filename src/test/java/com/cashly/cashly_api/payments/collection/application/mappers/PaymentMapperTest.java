package com.cashly.cashly_api.payments.collection.application.mappers;

import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.collection.domain.Payment;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("PaymentMapper Unit Tests")
class PaymentMapperTest {

    private final PaymentId paymentId = PaymentId.generate();
    private final PaymentNumber paymentNumber = PaymentNumber.of(2026, 1);
    private final UUID customerId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("500.00");
    private final UUID paymentMethodId = UUID.randomUUID();
    private final String referenceNumber = "REF-001";
    private final LocalDate paymentDate = LocalDate.of(2026, 6, 15);
    private final String notes = "Test payment";
    private final PaymentStatus status = PaymentStatus.APPLIED;
    private final BigDecimal appliedAmount = new BigDecimal("500.00");
    private final UUID createdBy = UUID.randomUUID();
    private final String createdByName = "Test User";
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final LocalDateTime updatedAt = LocalDateTime.now();

    private Payment createPayment() {
        return Payment.reconstitute(
                paymentId, paymentNumber, customerId, amount,
                paymentMethodId, PaymentSource.PAYMENT_METHOD,
                referenceNumber, paymentDate, notes,
                status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt
        );
    }

    @Test
    void should_MapPaymentToPaymentResponse_When_ValidPayment() {
        Payment payment = createPayment();

        PaymentResponse response = PaymentMapper.toResponse(payment);

        assertNotNull(response);
        assertEquals(paymentId.value(), response.id());
        assertEquals(paymentNumber.value(), response.paymentNumber());
        assertEquals(customerId, response.customerId());
        assertEquals(amount, response.amount());
        assertEquals(paymentMethodId, response.paymentMethodId());
        assertEquals(referenceNumber, response.referenceNumber());
        assertEquals(paymentDate, response.paymentDate());
        assertEquals(notes, response.notes());
        assertEquals(status.name(), response.status());
        assertEquals(appliedAmount, response.appliedAmount());
        assertEquals(createdBy, response.createdBy());
        assertEquals(createdByName, response.createdByName());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }

    @Test
    void should_MapPaymentToPaymentListResponse_When_ValidPayment() {
        Payment payment = createPayment();

        PaymentListResponse response = PaymentMapper.toListResponse(payment);

        assertNotNull(response);
        assertEquals(paymentId.value(), response.id());
        assertEquals(paymentNumber.value(), response.paymentNumber());
        assertEquals(customerId, response.customerId());
        assertEquals(amount, response.amount());
        assertEquals(paymentMethodId, response.paymentMethodId());
        assertEquals(referenceNumber, response.referenceNumber());
        assertEquals(paymentDate, response.paymentDate());
        assertEquals(status.name(), response.status());
        assertEquals(appliedAmount, response.appliedAmount());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void should_MapPaymentWithNullOptionalFields_When_SomeFieldsAreNull() {
        Payment payment = Payment.reconstitute(
                paymentId, paymentNumber, customerId, amount,
                null, PaymentSource.PAYMENT_METHOD,
                null, paymentDate, null,
                status, BigDecimal.ZERO,
                createdBy, createdByName, createdAt, updatedAt
        );

        PaymentResponse response = PaymentMapper.toResponse(payment);

        assertNotNull(response);
        assertEquals(paymentId.value(), response.id());
        assertEquals(amount, response.amount());
    }

    @Test
    void should_MapPaymentWithCancelledStatus_When_StatusIsCancelled() {
        Payment payment = Payment.reconstitute(
                paymentId, paymentNumber, customerId, amount,
                paymentMethodId, PaymentSource.PAYMENT_METHOD,
                referenceNumber, paymentDate, notes,
                PaymentStatus.CANCELLED, BigDecimal.ZERO,
                createdBy, createdByName, createdAt, updatedAt
        );

        PaymentResponse response = PaymentMapper.toResponse(payment);

        assertEquals("CANCELLED", response.status());
    }

}
