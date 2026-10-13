package com.cashly.cashly_api.reports.stock.application.usecases;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.reports.stock.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.reports.stock.application.dto.query.StockPdfQuery;

import java.util.List;

public class GenerateStockPdfUseCaseImpl implements GenerateStockPdfUseCase {

    private final StockReportRepositoryPort repository;
    private final StockPdfPort pdfPort;

    public GenerateStockPdfUseCaseImpl(StockReportRepositoryPort repository, StockPdfPort pdfPort) {
        this.repository = repository;
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(StockPdfQuery query) {
        List<StockReportResponse> items = repository.findAllForPdf(query);
        return pdfPort.generatePdf(items);
    }
}
