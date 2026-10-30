package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.adapters;

import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.repositories.StockReportJpaRepository;
import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections.StockReportProjection;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.StockPdfQuery;

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
        String status = p.getQuantity() == 0 ? "OUT_OF_STOCK" : "IN_STOCK";
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
