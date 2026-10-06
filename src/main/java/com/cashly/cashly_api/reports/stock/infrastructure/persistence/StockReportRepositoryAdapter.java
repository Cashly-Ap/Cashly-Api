package com.cashly.cashly_api.reports.stock.infrastructure.persistence;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.reports.stock.application.port.StockReportRepositoryPort;
import com.cashly.cashly_api.reports.stock.application.query.StockPdfQuery;

import java.util.List;

public class StockReportRepositoryAdapter implements StockReportRepositoryPort {

    private final StockReportJpaRepository jpaRepository;

    public StockReportRepositoryAdapter(StockReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<StockReportResponse> findAllForPdf(StockPdfQuery query) {
        List<StockReportProjection> projections = jpaRepository.findAllForPdf(
                query.warehouseId() != null ? query.warehouseId().toString() : null,
                query.categoryId() != null ? query.categoryId().toString() : null,
                query.withStock(),
                query.lowStock()
        );
        return projections.stream().map(this::toResponse).toList();
    }

    private StockReportResponse toResponse(StockReportProjection p) {
        String status = p.getQuantity() == 0 ? "Sin Stock" : "Con Stock";
        return new StockReportResponse(
                p.getWarehouseName(),
                p.getProductName(),
                p.getSku(),
                p.getCategoryName(),
                p.getQuantity(),
                status
        );
    }
}
