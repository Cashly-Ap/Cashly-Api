package com.parsun.parsun_api.reports.stock.infrastructure.config;

import com.parsun.parsun_api.reports.stock.application.port.StockExcelPort;
import com.parsun.parsun_api.reports.stock.application.port.StockPdfPort;
import com.parsun.parsun_api.reports.stock.application.port.StockReportRepositoryPort;
import com.parsun.parsun_api.reports.stock.application.usecases.GenerateStockExcelUseCase;
import com.parsun.parsun_api.reports.stock.application.usecases.GenerateStockExcelUseCaseImpl;
import com.parsun.parsun_api.reports.stock.application.usecases.GenerateStockPdfUseCase;
import com.parsun.parsun_api.reports.stock.application.usecases.GenerateStockPdfUseCaseImpl;
import com.parsun.parsun_api.reports.stock.infrastructure.excel.StockExcelGenerator;
import com.parsun.parsun_api.reports.stock.infrastructure.pdf.StockPdfGenerator;
import com.parsun.parsun_api.reports.stock.infrastructure.persistence.StockReportJpaRepository;
import com.parsun.parsun_api.reports.stock.infrastructure.persistence.StockReportRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockReportConfig {

    @Bean
    public StockReportRepositoryPort stockReportRepositoryPort(
            StockReportJpaRepository stockReportJpaRepository) {
        return new StockReportRepositoryAdapter(stockReportJpaRepository);
    }

    @Bean
    public StockPdfPort stockPdfPort() {
        return new StockPdfGenerator();
    }

    @Bean
    public StockExcelPort stockExcelPort() {
        return new StockExcelGenerator();
    }

    @Bean
    public GenerateStockPdfUseCase generateStockPdfUseCase(
            StockReportRepositoryPort stockReportRepositoryPort,
            StockPdfPort stockPdfPort) {
        return new GenerateStockPdfUseCaseImpl(stockReportRepositoryPort, stockPdfPort);
    }

    @Bean
    public GenerateStockExcelUseCase generateStockExcelUseCase(
            StockReportRepositoryPort stockReportRepositoryPort,
            StockExcelPort stockExcelPort) {
        return new GenerateStockExcelUseCaseImpl(stockReportRepositoryPort, stockExcelPort);
    }
}
