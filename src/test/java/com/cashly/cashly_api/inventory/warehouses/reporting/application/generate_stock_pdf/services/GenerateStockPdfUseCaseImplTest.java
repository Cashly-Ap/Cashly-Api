package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_pdf.services;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.StockPdfQuery;
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
@DisplayName("GenerateStockPdfUseCaseImpl Unit Tests")
class GenerateStockPdfUseCaseImplTest {

    @Mock
    private StockReportRepositoryPort repository;

    @Mock
    private StockPdfPort pdfPort;

    @InjectMocks
    private GenerateStockPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate stock PDF from query")
    void should_generate_stock_pdf() {
        StockPdfQuery query = new StockPdfQuery(
                UUID.randomUUID(), UUID.randomUUID(), true, true);
        List<StockReportResponse> items = List.of(
                new StockReportResponse("Warehouse", "Product", "SKU001", "Category", 0, "Sin Stock"));
        byte[] expectedPdf = "Stock PDF".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repository).findAllForPdf(query);
        verify(pdfPort).generatePdf(items);
    }

    @Test
    @DisplayName("should generate stock PDF with empty result")
    void should_generate_stock_pdf_with_empty_result() {
        StockPdfQuery query = new StockPdfQuery(null, null, null, null);
        List<StockReportResponse> items = List.of();
        byte[] expectedPdf = "Empty PDF".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repository).findAllForPdf(query);
        verify(pdfPort).generatePdf(items);
    }
}
