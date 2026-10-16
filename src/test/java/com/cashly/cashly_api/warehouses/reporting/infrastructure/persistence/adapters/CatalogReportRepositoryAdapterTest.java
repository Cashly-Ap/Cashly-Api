package com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.adapters;

import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories.CatalogReportJpaRepository;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.projections.CatalogReportProjection;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogReportRepositoryAdapter Unit Tests")
class CatalogReportRepositoryAdapterTest {

    @Mock
    private CatalogReportJpaRepository jpaRepository;

    @InjectMocks
    private CatalogReportRepositoryAdapter adapter;

    @Test
    @DisplayName("should find catalog and map to response")
    void should_find_catalog() {
        CatalogReportQuery query = new CatalogReportQuery(
                "search", UUID.randomUUID(), UUID.randomUUID(), true, true, false, 0, 20);

        CatalogReportProjection projection = mock(CatalogReportProjection.class);
        when(projection.getProductId()).thenReturn(UUID.randomUUID().toString());
        when(projection.getProductName()).thenReturn("Product A");
        when(projection.getSku()).thenReturn("SKU001");
        when(projection.getCategoryId()).thenReturn(UUID.randomUUID().toString());
        when(projection.getCategoryName()).thenReturn("Category");
        when(projection.getComposite()).thenReturn(false);
        when(projection.getWarehouseId()).thenReturn(UUID.randomUUID().toString());
        when(projection.getWarehouseName()).thenReturn("Warehouse");
        when(projection.getQuantity()).thenReturn(BigDecimal.TEN);
        when(projection.getActive()).thenReturn(true);

        when(jpaRepository.findCatalog(any(), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(projection)));

        PageResponse<CatalogReportResponse> result = adapter.findCatalog(query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Product A", result.content().get(0).productName());
        assertEquals("SKU001", result.content().get(0).sku());
        assertTrue(result.content().get(0).active());
        verify(jpaRepository).findCatalog(any(), any(), any(), any(), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("should find catalog with null filters")
    void should_find_catalog_with_null_filters() {
        CatalogReportQuery query = new CatalogReportQuery(
                null, null, null, null, null, null, 0, 20);

        when(jpaRepository.findCatalog(any(), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponse<CatalogReportResponse> result = adapter.findCatalog(query);

        assertTrue(result.content().isEmpty());
        verify(jpaRepository).findCatalog(any(), any(), any(), any(), any(), any(PageRequest.class));
    }
}
