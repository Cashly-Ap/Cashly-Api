package com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf.services;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.CatalogPdfQuery;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateCatalogPdfUseCaseImpl Unit Tests")
class GenerateCatalogPdfUseCaseImplTest {

    @Mock
    private CatalogReportRepositoryPort catalogReportRepositoryPort;

    @Mock
    private CatalogPdfPort catalogPdfPort;

    @InjectMocks
    private GenerateCatalogPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate catalog PDF from query")
    void should_generate_catalog_pdf() {
        CatalogPdfQuery query = new CatalogPdfQuery(
                "search", UUID.randomUUID(), UUID.randomUUID(), true, true, false);
        List<CatalogReportResponse> items = List.of(
                new CatalogReportResponse(UUID.randomUUID(), "Product A", "SKU001",
                        UUID.randomUUID(), "Category", false,
                        UUID.randomUUID(), "Warehouse", BigDecimal.TEN, true));
        byte[] expectedPdf = "Catalog PDF".getBytes();

        when(catalogReportRepositoryPort.findCatalog(any(CatalogReportQuery.class)))
                .thenReturn(PageResponseMapper.from(new PageImpl<>(items)));
        when(catalogPdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(catalogReportRepositoryPort).findCatalog(any(CatalogReportQuery.class));
        verify(catalogPdfPort).generatePdf(items);
    }

    @Test
    @DisplayName("should generate catalog PDF with empty result")
    void should_generate_catalog_pdf_with_empty_result() {
        CatalogPdfQuery query = new CatalogPdfQuery(
                null, null, null, null, null, null);
        List<CatalogReportResponse> items = List.of();
        byte[] expectedPdf = "Empty PDF".getBytes();

        when(catalogReportRepositoryPort.findCatalog(any(CatalogReportQuery.class)))
                .thenReturn(PageResponseMapper.from(new PageImpl<>(items)));
        when(catalogPdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(catalogReportRepositoryPort).findCatalog(any(CatalogReportQuery.class));
        verify(catalogPdfPort).generatePdf(items);
    }
}
