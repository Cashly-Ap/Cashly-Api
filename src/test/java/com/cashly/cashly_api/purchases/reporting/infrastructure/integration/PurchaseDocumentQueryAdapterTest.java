package com.cashly.cashly_api.purchases.reporting.infrastructure.integration;
import com.cashly.cashly_api.purchases.application.queries.services.PurchaseDocumentQueryService;
import com.cashly.cashly_api.purchases.application.list.use_case.ListPurchasesUseCase;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.purchases.reporting.application.dto.model.PurchaseReportFilter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PurchaseDocumentQueryAdapterTest {
    private final ListPurchasesUseCase list = mock(ListPurchasesUseCase.class);
    private final PurchaseDocumentQueryAdapter adapter = new PurchaseDocumentQueryAdapter(new PurchaseDocumentQueryService(mock(PurchaseRepository.class), list));
    @Test
    void listPreservesAllFiltersOrderingAndAmounts() {
        var filter = new PurchaseReportFilter(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), UUID.randomUUID(), "DRAFT", UUID.randomUUID(), UUID.randomUUID());
        var source = mock(PurchaseListResponse.class);
        var id = UUID.randomUUID();
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        when(source.id()).thenReturn(id);
        when(source.purchaseNumber()).thenReturn("DOC-123");
        when(source.supplierName()).thenReturn("Customer");
        when(source.status()).thenReturn("DRAFT");
        when(source.subtotal()).thenReturn(new BigDecimal("31.25"));
        when(source.createdAt()).thenReturn(date);
        
        when(list.execute(filter.dateFrom(), filter.dateTo(), filter.supplierId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")).thenReturn(List.of(source));
        var result = adapter.findAll(filter);
        var row = result.getFirst();
        assertEquals(id, row.id());
        assertEquals("DOC-123", row.purchaseNumber());
        assertEquals("Customer", row.supplierName());
        assertEquals("DRAFT", row.status());
        assertEquals(new BigDecimal("31.25"), row.subtotal());
        assertEquals(date, row.createdAt());
        
        assertThrows(UnsupportedOperationException.class, result::clear);
        verify(list).execute(filter.dateFrom(), filter.dateTo(), filter.supplierId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc");
    }
    @Test
    void emptyUnfilteredListPreservesNullFilterSemantics() {
        when(list.execute(null, null, null, null, null, null, "createdAt", "desc")).thenReturn(List.of());
        assertTrue(adapter.findAll(new PurchaseReportFilter(null, null, null, null, null, null)).isEmpty());
        verify(list).execute(null, null, null, null, null, null, "createdAt", "desc");
    }
    @Test void singlePurchasePreservesLinesAmountsAndMissingError() {
        var repository = mock(PurchaseRepository.class);
        var id = UUID.randomUUID();
        var key = com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId.of(id);
        var purchase = mock(com.cashly.cashly_api.purchases.domain.entity.Purchase.class);
        when(purchase.getId()).thenReturn(key);
        when(purchase.getPurchaseNumber()).thenReturn(new com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber("PUR-123"));
        when(purchase.getStatus()).thenReturn(com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus.DRAFT);
        when(purchase.getSupplierName()).thenReturn("Supplier");
        when(purchase.getSubtotal()).thenReturn(new BigDecimal("31.25"));
        when(purchase.getLines()).thenReturn(List.of());
        when(repository.findById(key)).thenReturn(java.util.Optional.of(purchase));
        var adapter = new PurchaseDocumentQueryAdapter(new PurchaseDocumentQueryService(repository, list));
        var result = adapter.find(id);
        assertEquals(id, result.id());
        assertEquals("PUR-123", result.purchaseNumber());
        assertEquals("Supplier", result.supplierName());
        assertEquals(new BigDecimal("31.25"), result.subtotal());
        assertTrue(result.lines().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.lines().clear());
        when(repository.findById(key)).thenReturn(java.util.Optional.empty());
        assertThrows(com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException.class, () -> adapter.find(id));
    }    @Test void singleProjectionPreservesFractionalQuantityCostAndLineSubtotal() {
        var repository = mock(PurchaseRepository.class);
        var id = UUID.randomUUID();
        var key = com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId.of(id);
        var purchase = mock(com.cashly.cashly_api.purchases.domain.entity.Purchase.class);
        when(purchase.getId()).thenReturn(key);
        when(purchase.getPurchaseNumber()).thenReturn(new com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber("PUR-LINE"));
        when(purchase.getStatus()).thenReturn(com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus.CANCELLED);
        when(purchase.getSupplierName()).thenReturn("Supplier");
        when(purchase.getSupplierRif()).thenReturn("RIF-123");
        when(purchase.getNotes()).thenReturn("Notes");
        when(purchase.getCancelledReason()).thenReturn("Reason");
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        when(purchase.getCreatedAt()).thenReturn(date);
        when(purchase.getSubtotal()).thenReturn(new BigDecimal("3.125"));
        var line = mock(com.cashly.cashly_api.purchases.domain.entity.PurchaseLine.class);
        when(line.getId()).thenReturn(com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseLineId.generate());
        when(line.getProductId()).thenReturn(UUID.randomUUID());
        when(line.getProductCode()).thenReturn("SKU-123");
        when(line.getProductName()).thenReturn("Product");
        when(line.getQuantity()).thenReturn(new BigDecimal("1.25"));
        when(line.getUnitCost()).thenReturn(new BigDecimal("2.50"));
        when(line.getLineTotal()).thenReturn(new BigDecimal("3.125"));
        when(line.getAllocations()).thenReturn(List.of());
        when(purchase.getLines()).thenReturn(List.of(line));
        when(repository.findById(key)).thenReturn(java.util.Optional.of(purchase));
        var result = new PurchaseDocumentQueryAdapter(new PurchaseDocumentQueryService(repository, list)).find(id);
        assertEquals("RIF-123", result.supplierRif());
        assertEquals("Notes", result.notes());
        assertEquals("Reason", result.cancelledReason());
        assertEquals(date, result.createdAt());
        assertEquals("CANCELLED", result.status());
        assertEquals("SKU-123", result.lines().getFirst().productCode());
        assertEquals("Product", result.lines().getFirst().productName());
        assertEquals(new BigDecimal("1.25"), result.lines().getFirst().quantity());
        assertEquals(new BigDecimal("2.50"), result.lines().getFirst().unitCost());
        assertEquals(new BigDecimal("3.125"), result.lines().getFirst().subtotal());
    }}