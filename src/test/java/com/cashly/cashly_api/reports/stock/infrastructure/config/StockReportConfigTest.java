package com.cashly.cashly_api.reports.stock.infrastructure.config;

import com.cashly.cashly_api.reports.stock.application.port.StockExcelPort;
import com.cashly.cashly_api.reports.stock.application.port.StockPdfPort;
import com.cashly.cashly_api.reports.stock.application.port.StockReportRepositoryPort;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockExcelUseCase;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockPdfUseCase;
import com.cashly.cashly_api.reports.stock.infrastructure.persistence.StockReportJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReportConfig Unit Tests")
class StockReportConfigTest {

    @Mock
    private StockReportJpaRepository stockReportJpaRepository;

    @Mock
    private StockReportRepositoryPort stockReportRepositoryPort;

    @Mock
    private StockPdfPort stockPdfPort;

    @Mock
    private StockExcelPort stockExcelPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new StockReportConfig();

        var pdfUseCase = config.generateStockPdfUseCase(stockReportRepositoryPort, stockPdfPort);
        assertNotNull(pdfUseCase);

        var excelUseCase = config.generateStockExcelUseCase(stockReportRepositoryPort, stockExcelPort);
        assertNotNull(excelUseCase);
    }

    @Test
    @DisplayName("should create port and adapter beans")
    void should_create_port_and_adapter_beans() {
        var config = new StockReportConfig();

        assertNotNull(config.stockReportRepositoryPort(stockReportJpaRepository));
        assertNotNull(config.stockPdfPort());
        assertNotNull(config.stockExcelPort());
    }
}
