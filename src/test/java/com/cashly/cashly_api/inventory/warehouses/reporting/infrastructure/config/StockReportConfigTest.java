package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockExcelPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_excel.use_case.GenerateStockExcelUseCase;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_pdf.use_case.GenerateStockPdfUseCase;
import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.repositories.StockReportJpaRepository;
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

        var pdfUseCase = config.generateStockPdfUseCase(
                stockReportRepositoryPort, stockPdfPort);
        assertNotNull(pdfUseCase);

        var excelUseCase = config.generateStockExcelUseCase(
                stockReportRepositoryPort, stockExcelPort);
        assertNotNull(excelUseCase);
    }

    @Test
    @DisplayName("should create port and adapter beans")
    void should_create_port_and_adapter_beans() {
        var config = new StockReportConfig();

        assertNotNull(
                config.stockReportRepositoryPort(stockReportJpaRepository));
        assertNotNull(config.stockPdfPort(new PdfBranding()));
        assertNotNull(config.stockExcelPort());
    }
}
