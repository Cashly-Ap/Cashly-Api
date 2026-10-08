package com.cashly.cashly_api.reports.finance.infrastructure.integration;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.application.services.PaymentDocumentQueryService;
import com.cashly.cashly_api.payments.application.usecases.GetPaymentByIdUseCase;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PaymentDocumentQueryAdapterTest {
    private final GetPaymentByIdUseCase query = mock(GetPaymentByIdUseCase.class);
    private final PaymentDocumentQueryAdapter adapter = new PaymentDocumentQueryAdapter(new PaymentDocumentQueryService(query));
    @Test
    void providerAndAdapterPreserveEveryRenderedFieldWithoutRecalculatingAmount() {
        var id = UUID.randomUUID();
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        var source = mock(PaymentResponse.class);
        when(source.id()).thenReturn(id);
        when(source.paymentNumber()).thenReturn("PAY-123");
        when(source.amount()).thenReturn(new BigDecimal("123.45"));
        when(source.referenceNumber()).thenReturn("REF-123");
        when(source.notes()).thenReturn("Notes");
        when(source.status()).thenReturn("VERIFIED");
        when(source.createdAt()).thenReturn(date);
        when(query.execute(id)).thenReturn(source);
        var result = adapter.find(id);
        assertEquals(id, result.id());
        assertEquals("PAY-123", result.paymentNumber());
        assertEquals(new BigDecimal("123.45"), result.amount());
        assertEquals("REF-123", result.referenceNumber());
        assertEquals("Notes", result.notes());
        assertEquals("VERIFIED", result.status());
        assertEquals(date, result.createdAt());
        verify(query).execute(id);
    }
    @Test
    void optionalFieldsPreserveNullValuesForRendererDefaults() {
        var id = UUID.randomUUID();
        when(query.execute(id)).thenReturn(mock(PaymentResponse.class));
        var result = adapter.find(id);
        assertNull(result.referenceNumber());
        assertNull(result.notes());
        assertNull(result.status());
    }
    @Test
    void missingPaymentPreservesOriginalErrorIdentityAndMessage() {
        var id = UUID.randomUUID();
        var failure = new IllegalArgumentException("Payment not found: " + id);
        when(query.execute(id)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> adapter.find(id)));
    }
}
