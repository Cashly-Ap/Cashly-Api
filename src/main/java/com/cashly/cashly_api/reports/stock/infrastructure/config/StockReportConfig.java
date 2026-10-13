package com.cashly.cashly_api.reports.stock.infrastructure.config;

import com.cashly.cashly_api.reports.stock.application.ports.out.StockExcelPort;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockExcelUseCase;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockExcelUseCaseImpl;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockPdfUseCase;
import com.cashly.cashly_api.reports.stock.application.usecases.GenerateStockPdfUseCaseImpl;
import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.excel.StockExcelGenerator;
import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.pdf.StockPdfGenerator;
import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.persistence.repositories.StockReportJpaRepository;
import com.cashly.cashly_api.reports.stock.infrastructure.adapters.out.persistence.adapters.StockReportRepositoryAdapter;
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
