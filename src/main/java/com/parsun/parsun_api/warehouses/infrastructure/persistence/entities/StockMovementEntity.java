package com.parsun.parsun_api.warehouses.infrastructure.persistence.entities;

import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "stock_movements",
    indexes = {
        @Index(name = "idx_stock_movement_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_stock_movement_product", columnList = "product_id"),
        @Index(name = "idx_stock_movement_type", columnList = "movement_type"),
        @Index(name = "idx_stock_movement_batch", columnList = "transfer_batch_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StockMovementEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "warehouse_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "previous_stock", nullable = false)
    private int previousStock;

    @Column(name = "new_stock", nullable = false)
    private int newStock;

    @Column(name = "origin_movement_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID originMovementId;

    @Column(name = "transfer_batch_id", length = 50)
    private String transferBatchId;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static StockMovementEntity fromDomain(StockMovement m) {
        StockMovementEntity e = new StockMovementEntity();
        e.id = (m.getId() != null) ? m.getId() : UUID.randomUUID();
        e.warehouseId = m.getWarehouseId();
        e.productId = m.getProductId();
        e.movementType = m.getMovementType();
        e.quantity = m.getQuantity();
        e.previousStock = m.getPreviousStock();
        e.newStock = m.getNewStock();
        e.originMovementId = m.getOriginMovementId();
        e.transferBatchId = m.getTransferBatchId();
        e.notes = m.getNotes();
        e.createdAt = m.getCreatedAt();
        return e;
    }

    public StockMovement toDomain() {
        return new StockMovement(id, warehouseId, productId, movementType,
                quantity, previousStock, newStock, originMovementId,
                transferBatchId, notes, createdAt);
    }
}
