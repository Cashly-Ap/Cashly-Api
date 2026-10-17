package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.web;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.CatalogPdfQuery;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_catalog_pdf.use_case.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.get_catalog_report.use_case.GetCatalogReportUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogReportController Unit Tests")
class CatalogReportControllerTest {

    @Mock
    private GetCatalogReportUseCase getCatalogReportUseCase;

    @Mock
    private GenerateCatalogPdfUseCase generateCatalogPdfUseCase;

    @InjectMocks
    private CatalogReportController controller;

    @Test
    @DisplayName("should return catalog report page")
    void should_get_catalog_report() {
        CatalogReportResponse item = new CatalogReportResponse(
                UUID.randomUUID(), "Product A", "SKU001",
                UUID.randomUUID(), "Category", false,
                UUID.randomUUID(), "Warehouse", BigDecimal.TEN, true);

        List<CatalogReportResponse> items = List.of(item);
        when(getCatalogReportUseCase.execute(any())).thenReturn(
                PageResponseMapper.from(new PageImpl<>(items)));

        PageResponse<CatalogReportResponse> result = controller.getCatalog(
                "search", UUID.randomUUID(), UUID.randomUUID(),
                true, true, false, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Product A", result.content().get(0).productName());
        verify(getCatalogReportUseCase).execute(any());
    }

    @Test
    @DisplayName("should return catalog report without optional params")
    void should_get_catalog_report_without_optional_params() {
        List<CatalogReportResponse> items = List.of();
        when(getCatalogReportUseCase.execute(any())).thenReturn(
                PageResponseMapper.from(new PageImpl<>(items)));

        PageResponse<CatalogReportResponse> result = controller.getCatalog(
                null, null, null, null, null, null, 0, 20);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(getCatalogReportUseCase).execute(any());
    }

    @Test
    @DisplayName("should return catalog PDF as attachment")
    void should_get_catalog_pdf() {
        byte[] pdfContent = "PDF content".getBytes();

        when(generateCatalogPdfUseCase.execute(any(CatalogPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCatalogPdf(
                "search", UUID.randomUUID(), UUID.randomUUID(),
                true, true, false);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("reporte-productos.pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateCatalogPdfUseCase).execute(any(CatalogPdfQuery.class));
    }

    @Test
    @DisplayName("should return catalog PDF without optional params")
    void should_get_catalog_pdf_without_optional_params() {
        byte[] pdfContent = "PDF without filters".getBytes();

        when(generateCatalogPdfUseCase.execute(any(CatalogPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCatalogPdf(
                null, null, null, null, null, null);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateCatalogPdfUseCase).execute(any(CatalogPdfQuery.class));
    }
}
