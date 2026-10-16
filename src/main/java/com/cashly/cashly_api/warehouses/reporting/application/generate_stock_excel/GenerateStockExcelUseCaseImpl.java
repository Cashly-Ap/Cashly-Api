package com.cashly.cashly_api.warehouses.reporting.application.generate_stock_excel;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.StockExcelPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;

import java.util.List;

public class GenerateStockExcelUseCaseImpl implements GenerateStockExcelUseCase {

    private final StockReportRepositoryPort repository;
    private final StockExcelPort excelPort;

    public GenerateStockExcelUseCaseImpl(StockReportRepositoryPort repository, StockExcelPort excelPort) {
        this.repository = repository;
        this.excelPort = excelPort;
    }

    @Override
    public byte[] execute(StockPdfQuery query) {
        List<StockReportResponse> items = repository.findAllForPdf(query);
        return excelPort.generateExcel(items);
    }
}
