package com.cashly.cashly_api.invoices.reporting.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.invoices.application.queries.services.InvoiceDocumentQueryService;
import com.cashly.cashly_api.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.invoices.application.list.use_case.GetInvoicesUseCase;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportFilter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceDocumentQueryAdapterTest {
    private final GetInvoiceByIdUseCase single = mock(GetInvoiceByIdUseCase.class);
    private final GetInvoicesUseCase list = mock(GetInvoicesUseCase.class);
    private final InvoiceDocumentQueryAdapter adapter = new InvoiceDocumentQueryAdapter(new InvoiceDocumentQueryService(single, list));

    @Test
    void providerAndAdapterPreserveEveryDocumentFieldAndLineAmount() {
        var id = UUID.randomUUID();
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        var line = new InvoiceDetailResponse.InvoiceLineDetailResponse(UUID.randomUUID(), UUID.randomUUID(), "Product", "P01", new BigDecimal("2.25"), new BigDecimal("12.40"), new BigDecimal("27.90"), List.of(), List.of(), BigDecimal.ZERO);
        var source = new InvoiceDetailResponse(id, "INV01", UUID.randomUUID(), "Customer", "RIF01", UUID.randomUUID(), "Operator", UUID.randomUUID(), "ORD01", "CANCELLED", "Cancelled", "CREDIT", "Notes", new BigDecimal("27.90"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("27.90"), List.of(), date, "Reason", date, date, List.of(line), BigDecimal.ZERO, "NONE", List.of());
        when(single.execute(id)).thenReturn(source);
        var result = adapter.find(id);
        assertEquals(id, result.id());
        assertEquals("INV01", result.invoiceNumber());
        assertEquals("Customer", result.customerName());
        assertEquals("RIF01", result.customerRif());
        assertEquals("ORD01", result.orderNumber());
        assertEquals("CANCELLED", result.status());
        assertEquals("Notes", result.notes());
        assertEquals("Reason", result.cancelledReason());
        assertEquals(date, result.createdAt());
        assertEquals(source.subtotal(), result.subtotal());
        assertEquals("CREDIT", result.paymentType());
        assertEquals(source.total(), result.total());
        assertEquals("P01", result.lines().getFirst().productCode());
        assertEquals("Product", result.lines().getFirst().productName());
        assertEquals(line.quantity(), result.lines().getFirst().quantity());
        assertEquals(line.unitPrice(), result.lines().getFirst().unitPrice());
        assertEquals(line.lineTotal(), result.lines().getFirst().lineTotal());
        assertThrows(UnsupportedOperationException.class, () -> result.lines().clear());
        verify(single).execute(id);
    }

    @Test
    void listPreservesFiltersSortAndRowsWithoutRecalculatingTotals() {
        var filter = new InvoiceReportFilter(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), UUID.randomUUID(), List.of("PAID"), UUID.randomUUID(), UUID.randomUUID());
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        var source = new InvoiceListResponse(UUID.randomUUID(), "INV02", filter.customerId(), "Customer", "PAID", "Paid", "CASH", new BigDecimal("30"), new BigDecimal("31.25"), new BigDecimal("31.25"), BigDecimal.ZERO, BigDecimal.ZERO, 2, null, null, date, "NONE");
        when(list.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.statuses(), null, filter.productId(), filter.categoryId(), "createdAt", "desc")).thenReturn(List.of(source));
        var rows = adapter.findAll(filter);
        var row = rows.getFirst();
        assertEquals(source.id(), row.id());
        assertEquals(source.invoiceNumber(), row.invoiceNumber());
        assertEquals(source.customerName(), row.customerName());
        assertEquals(source.paymentType(), row.paymentType());
        assertEquals(source.lineCount(), row.lineCount());
        assertEquals(source.status(), row.status());
        assertEquals(source.total(), row.total());
        assertEquals(source.createdAt(), row.createdAt());
        assertThrows(UnsupportedOperationException.class, rows::clear);
        verify(list).execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.statuses(), null, filter.productId(), filter.categoryId(), "createdAt", "desc");
    }

    @Test
    void missingInvoicePreservesProviderFailure() {
        var id = UUID.randomUUID();
        var failure = new IllegalArgumentException("Missing invoice");
        when(single.execute(id)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> adapter.find(id)));
    }

    @Test
    void unfilteredEmptyReportPreservesNullFilterSemantics() {
        when(list.execute(null, null, null, null, null, null, null, "createdAt", "desc")).thenReturn(List.of());
        assertTrue(adapter.findAll(new InvoiceReportFilter(null, null, null, null, null, null)).isEmpty());
        verify(list).execute(null, null, null, null, null, null, null, "createdAt", "desc");
    }
}
