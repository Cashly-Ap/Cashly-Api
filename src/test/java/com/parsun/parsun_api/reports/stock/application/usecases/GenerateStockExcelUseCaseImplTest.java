package com.parsun.parsun_api.reports.stock.application.usecases;

import com.parsun.parsun_api.reports.stock.application.dto.response.StockReportResponse;
import com.parsun.parsun_api.reports.stock.application.port.StockExcelPort;
import com.parsun.parsun_api.reports.stock.application.port.StockReportRepositoryPort;
import com.parsun.parsun_api.reports.stock.application.query.StockPdfQuery;
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
@DisplayName("GenerateStockExcelUseCaseImpl Unit Tests")
class GenerateStockExcelUseCaseImplTest {

    @Mock
    private StockReportRepositoryPort repository;

    @Mock
    private StockExcelPort excelPort;

    @InjectMocks
    private GenerateStockExcelUseCaseImpl useCase;

    @Test
    @DisplayName("should generate stock Excel from query")
    void should_generate_stock_excel() {
        StockPdfQuery query = new StockPdfQuery(
                UUID.randomUUID(), UUID.randomUUID(), true, false);
        List<StockReportResponse> items = List.of(
                new StockReportResponse("Warehouse", "Product", "SKU001", "Category", 10, "Con Stock"));
        byte[] expectedExcel = "Excel content".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(excelPort.generateExcel(items)).thenReturn(expectedExcel);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedExcel, result);
        verify(repository).findAllForPdf(query);
        verify(excelPort).generateExcel(items);
    }

    @Test
    @DisplayName("should generate stock Excel with empty result")
    void should_generate_stock_excel_with_empty_result() {
        StockPdfQuery query = new StockPdfQuery(null, null, null, null);
        List<StockReportResponse> items = List.of();
        byte[] expectedExcel = "Empty Excel".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(excelPort.generateExcel(items)).thenReturn(expectedExcel);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedExcel, result);
        verify(repository).findAllForPdf(query);
        verify(excelPort).generateExcel(items);
    }
}
