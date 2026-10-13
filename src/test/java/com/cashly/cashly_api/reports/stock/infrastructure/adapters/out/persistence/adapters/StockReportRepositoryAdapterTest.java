package com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.persistence.repositories.StockReportJpaRepository;
import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.persistence.projections.StockReportProjection;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.reports.stock.application.dto.query.StockPdfQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReportRepositoryAdapter Unit Tests")
class StockReportRepositoryAdapterTest {

    @Mock
    private StockReportJpaRepository jpaRepository;

    @InjectMocks
    private StockReportRepositoryAdapter adapter;

    @Test
    @DisplayName("should find stock and map to response with status")
    void should_find_stock() {
        StockPdfQuery query = new StockPdfQuery(
                UUID.randomUUID(), UUID.randomUUID(), true, false);

        StockReportProjection projection = mock(StockReportProjection.class);
        when(projection.getWarehouseName()).thenReturn("Main Warehouse");
        when(projection.getProductName()).thenReturn("Product A");
        when(projection.getSku()).thenReturn("SKU001");
        when(projection.getCategoryName()).thenReturn("Electronics");
        when(projection.getQuantity()).thenReturn(50);

        when(jpaRepository.findAllForPdf(any(), any(), any(), any()))
                .thenReturn(List.of(projection));

        List<StockReportResponse> result = adapter.findAllForPdf(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.get(0).warehouseName());
        assertEquals("Product A", result.get(0).productName());
        assertEquals("Con Stock", result.get(0).status());
        verify(jpaRepository).findAllForPdf(any(), any(), any(), any());
    }

    @Test
    @DisplayName("should mark zero quantity as Sin Stock")
    void should_mark_zero_quantity_as_sin_stock() {
        StockPdfQuery query = new StockPdfQuery(null, null, null, null);

        StockReportProjection projection = mock(StockReportProjection.class);
        when(projection.getWarehouseName()).thenReturn("Warehouse");
        when(projection.getProductName()).thenReturn("Product");
        when(projection.getSku()).thenReturn("SKU");
        when(projection.getCategoryName()).thenReturn("Category");
        when(projection.getQuantity()).thenReturn(0);

        when(jpaRepository.findAllForPdf(any(), any(), any(), any()))
                .thenReturn(List.of(projection));

        List<StockReportResponse> result = adapter.findAllForPdf(query);

        assertEquals("Sin Stock", result.get(0).status());
    }

    @Test
    @DisplayName("should return empty list for no results")
    void should_return_empty_list_for_no_results() {
        StockPdfQuery query = new StockPdfQuery(null, null, null, null);

        when(jpaRepository.findAllForPdf(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<StockReportResponse> result = adapter.findAllForPdf(query);

        assertTrue(result.isEmpty());
    }
}
