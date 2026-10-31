package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_pdf.services;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_pdf.use_case.GenerateStockPdfUseCase;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.StockPdfQuery;

import java.util.List;

public class GenerateStockPdfUseCaseImpl implements GenerateStockPdfUseCase {

    private final StockReportRepositoryPort repository;
    private final StockPdfPort pdfPort;

    public GenerateStockPdfUseCaseImpl(StockReportRepositoryPort repository,
            StockPdfPort pdfPort) {
        this.repository = repository;
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(StockPdfQuery query) {
        List<StockReportResponse> items = repository.findAllForPdf(query);
        return pdfPort.generatePdf(items);
    }
}
