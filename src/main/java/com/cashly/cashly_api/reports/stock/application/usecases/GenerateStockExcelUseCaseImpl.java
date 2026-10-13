package com.cashly.cashly_api.reports.stock.application.usecases;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockExcelPort;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.reports.stock.application.dto.query.StockPdfQuery;

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
