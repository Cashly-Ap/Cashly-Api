package com.cashly.cashly_api.payments.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreatePaymentRequest Unit Tests")
class CreatePaymentRequestTest {

    @Test
    void should_CreateRequest_WithAllFields() {
        UUID customerId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        UUID targetInvoiceId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                customerId, "APPLIED", new BigDecimal("500.00"),
                paymentMethodId, "REF-001", LocalDate.of(2026, 6, 15), "Test payment",
                targetInvoiceId, "payments/justificantes/abc.pdf"
        );

        assertEquals(customerId, request.customerId());
        assertEquals("APPLIED", request.status());
        assertEquals(new BigDecimal("500.00"), request.amount());
        assertEquals(paymentMethodId, request.paymentMethodId());
        assertEquals("REF-001", request.referenceNumber());
        assertEquals(LocalDate.of(2026, 6, 15), request.paymentDate());
        assertEquals("Test payment", request.notes());
        assertEquals(targetInvoiceId, request.targetInvoiceId());
        assertEquals("payments/justificantes/abc.pdf", request.voucherPath());
    }

    @Test
    void should_CreateRequest_WithNullNotesAndReference() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                UUID.randomUUID(), "APPLIED", BigDecimal.TEN,
                UUID.randomUUID(), null, LocalDate.now(), null,
                null, null
        );

        assertNull(request.notes());
        assertNull(request.referenceNumber());
    }

    @Test
    void should_AllowNullTargetInvoiceIdAndVoucherPath() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                UUID.randomUUID(), "PENDING", BigDecimal.TEN,
                UUID.randomUUID(), "REF-001", LocalDate.now(), null,
                null, null
        );

        assertNull(request.targetInvoiceId());
        assertNull(request.voucherPath());
    }

    @Test
    void should_BeRecord() {
        assertTrue(CreatePaymentRequest.class.isRecord());
    }
}
