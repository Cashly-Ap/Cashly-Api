package com.cashly.cashly_api.reports.inventory.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.reports.inventory.application.dto.query.CommercialCatalogQuery;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommercialCatalogController Unit Tests")
class CommercialCatalogControllerTest {

    @Mock
    private GenerateCommercialCatalogPdfUseCase generateCommercialCatalogPdfUseCase;

    @InjectMocks
    private CommercialCatalogController controller;

    @Test
    @DisplayName("should return commercial catalog PDF as attachment")
    void should_get_commercial_catalog_pdf() {
        byte[] pdfContent = "Commercial catalog PDF".getBytes();

        when(generateCommercialCatalogPdfUseCase.execute(any(CommercialCatalogQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCatalogPdf(
                "search", UUID.randomUUID(), UUID.randomUUID(), true);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("catalogo-comercial.pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateCommercialCatalogPdfUseCase).execute(any(CommercialCatalogQuery.class));
    }

    @Test
    @DisplayName("should return commercial catalog PDF without optional params")
    void should_get_commercial_catalog_pdf_without_optional_params() {
        byte[] pdfContent = "PDF without filters".getBytes();

        when(generateCommercialCatalogPdfUseCase.execute(any(CommercialCatalogQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getCatalogPdf(null, null, null, null);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateCommercialCatalogPdfUseCase).execute(any(CommercialCatalogQuery.class));
    }
}
