package com.cashly.cashly_api.warehouses.reporting.infrastructure.web;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;
import com.cashly.cashly_api.warehouses.reporting.application.generate_stock_excel.GenerateStockExcelUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_stock_pdf.GenerateStockPdfUseCase;
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
@DisplayName("StockReportController Unit Tests")
class StockReportControllerTest {

    @Mock
    private GenerateStockPdfUseCase generateStockPdfUseCase;

    @Mock
    private GenerateStockExcelUseCase generateStockExcelUseCase;

    @InjectMocks
    private StockReportController controller;

    @Test
    @DisplayName("should return stock PDF as attachment")
    void should_get_stock_pdf() {
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] pdfContent = "Stock PDF".getBytes();

        when(generateStockPdfUseCase.execute(any(StockPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getStockPdf(
                warehouseId, categoryId, true, false);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("stock-almacenes.pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateStockPdfUseCase).execute(any(StockPdfQuery.class));
    }

    @Test
    @DisplayName("should return stock PDF without optional params")
    void should_get_stock_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();

        when(generateStockPdfUseCase.execute(any(StockPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getStockPdf(
                null, null, null, null);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateStockPdfUseCase).execute(any(StockPdfQuery.class));
    }

    @Test
    @DisplayName("should return stock Excel as attachment")
    void should_get_stock_excel() {
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] excelContent = "Excel content".getBytes();

        when(generateStockExcelUseCase.execute(any(StockPdfQuery.class)))
                .thenReturn(excelContent);

        ResponseEntity<byte[]> result = controller.getStockExcel(
                warehouseId, categoryId, true, false);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("stock-almacenes.xlsx"));
        assertArrayEquals(excelContent, result.getBody());
        verify(generateStockExcelUseCase).execute(any(StockPdfQuery.class));
    }
}
