package com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.adapters;

import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories.MovementsReportJpaRepository;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.projections.MovementsReportProjection;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.MovementsPdfQuery;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MovementsReportRepositoryAdapter implements MovementsReportRepositoryPort {

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "ENTRY", "Entrada",
            "EXIT", "Salida",
            "ADJUSTMENT", "Ajuste",
            "TRANSFER_IN", "Transferencia Entrada",
            "TRANSFER_OUT", "Transferencia Salida",
            "COMPONENT_EXIT", "Salida Componente",
            "RETURN_ENTRY", "Entrada por Devolución",
            "CANCEL_ENTRY", "Entrada por Anulación",
            "SALE_EXIT", "Salida por Venta"
    );

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
        String label = TYPE_LABELS.getOrDefault(p.getMovementType(), p.getMovementType());
        return new MovementReportResponse(
                UUID.fromString(p.getMovementId()),
                p.getWarehouseName(),
                p.getProductName(),
                p.getSku(),
                label,
                p.getQuantity(),
                p.getPreviousStock(),
                p.getNewStock(),
                p.getNotes(),
                p.getCreatedAt()
        );
    }
}
