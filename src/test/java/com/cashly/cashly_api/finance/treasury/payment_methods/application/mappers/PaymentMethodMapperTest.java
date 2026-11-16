package com.cashly.cashly_api.finance.treasury.payment_methods.application.mappers;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMethodMapper Unit Tests")
class PaymentMethodMapperTest {

    private PaymentMethod createPaymentMethod(String code, String name,
            boolean active) {
        return PaymentMethod.reconstitute(PaymentMethodId.of(UUID.randomUUID()),
                code, name, "Test description", active,
                LocalDateTime.of(2026, 1, 15, 10, 0),
                LocalDateTime.of(2026, 1, 15, 10, 0));
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        void should_MapPaymentMethodToResponse() {
            PaymentMethod pm = createPaymentMethod("CASH", "Efectivo", true);

            PaymentMethodResponse response = PaymentMethodMapper.toResponse(pm,
                    false);

            assertEquals(pm.getId().value(), response.id());
            assertEquals("CASH", response.code());
            assertEquals("Efectivo", response.name());
            assertEquals("Test description", response.description());
            assertTrue(response.isActive());
            assertFalse(response.hasAssociatedRecords());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());
        }

        @Test
        void should_MapInactivePaymentMethod() {
            PaymentMethod pm = createPaymentMethod("CHQ", "Cheque", false);

            PaymentMethodResponse response = PaymentMethodMapper.toResponse(pm,
                    false);

            assertFalse(response.isActive());
        }

        @Test
        void should_MapWithNullDescription() {
            PaymentMethod pm = PaymentMethod.reconstitute(
                    PaymentMethodId.of(UUID.randomUUID()), "TRF",
                    "Transferencia", null, true, LocalDateTime.now(),
                    LocalDateTime.now());

            PaymentMethodResponse response = PaymentMethodMapper.toResponse(pm,
                    false);

            assertNull(response.description());
        }

        @Test
        void should_MapHasAssociatedRecordsTrue() {
            PaymentMethod pm = createPaymentMethod("CASH", "Efectivo", true);

            PaymentMethodResponse response = PaymentMethodMapper.toResponse(pm,
                    true);

            assertTrue(response.hasAssociatedRecords());
        }
    }

    @Nested
    @DisplayName("toResponseList")
    class ToResponseList {

        @Test
        void should_MapListOfPaymentMethods() {
            List<PaymentMethod> methods = List.of(
                    createPaymentMethod("CASH", "Efectivo", true),
                    createPaymentMethod("CHQ", "Cheque", true),
                    createPaymentMethod("TRF", "Transferencia", false));

            List<PaymentMethodResponse> responses = PaymentMethodMapper
                    .toResponseList(methods, pm -> false);

            assertEquals(3, responses.size());
            assertEquals("CASH", responses.get(0).code());
            assertEquals("CHQ", responses.get(1).code());
            assertEquals("TRF", responses.get(2).code());
        }

        @Test
        void should_ReturnEmptyList_When_InputIsEmpty() {
            List<PaymentMethodResponse> responses = PaymentMethodMapper
                    .toResponseList(List.of(), pm -> false);

            assertTrue(responses.isEmpty());
        }
    }
}
