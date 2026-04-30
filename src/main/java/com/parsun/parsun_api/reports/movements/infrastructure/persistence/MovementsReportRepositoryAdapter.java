package com.parsun.parsun_api.reports.movements.infrastructure.persistence;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;
import com.parsun.parsun_api.reports.movements.application.port.MovementsReportRepositoryPort;
import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;

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
            "COMPONENT_EXIT", "Salida Componente"
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
