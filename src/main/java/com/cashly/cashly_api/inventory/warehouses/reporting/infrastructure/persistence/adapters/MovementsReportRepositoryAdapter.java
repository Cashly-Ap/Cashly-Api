package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.adapters;

import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.repositories.MovementsReportJpaRepository;
import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections.MovementsReportProjection;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;

import java.util.List;
import java.util.UUID;

public class MovementsReportRepositoryAdapter implements MovementsReportRepositoryPort {

    private final MovementsReportJpaRepository jpaRepository;

    public MovementsReportRepositoryAdapter(MovementsReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<MovementReportResponse> findAllForPdf(MovementsPdfQuery query) {
        List<MovementsReportProjection> projections = jpaRepository.findAllForPdf(
                query.warehouseId() != null ? query.warehouseId().toString() : null,
                query.productId() != null ? query.productId().toString() : null,
                query.movementType(),
                query.dateFrom(),
                query.dateTo()
        );
        return projections.stream().map(this::toResponse).toList();
    }

    private MovementReportResponse toResponse(MovementsReportProjection p) {
        return new MovementReportResponse(
                UUID.fromString(p.getMovementId()),
                p.getWarehouseName(),
                p.getProductName(),
                p.getSku(),
                p.getMovementType(),
                p.getQuantity(),
                p.getPreviousStock(),
                p.getNewStock(),
                p.getNotes(),
                p.getCreatedAt()
        );
    }
}
