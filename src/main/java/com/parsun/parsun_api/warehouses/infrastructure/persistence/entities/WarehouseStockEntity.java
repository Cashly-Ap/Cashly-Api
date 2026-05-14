package com.parsun.parsun_api.warehouses.infrastructure.persistence.entities;

import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "warehouse_stock",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_warehouse_stock_product",
        columnNames = {"warehouse_id", "product_id"}
    ),
    indexes = {
        @Index(name = "idx_warehouse_stock_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_warehouse_stock_product", columnList = "product_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WarehouseStockEntity {

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

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "last_movement_at")
    private LocalDateTime lastMovementAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WarehouseStockEntity fromDomain(WarehouseStock s) {
        WarehouseStockEntity e = new WarehouseStockEntity();
        e.id = (s.getId() != null) ? s.getId() : UUID.randomUUID();
        e.warehouseId = s.getWarehouseId();
        e.productId = s.getProductId();
        e.quantity = s.getQuantity();
        e.reserved = s.getReserved();
        e.lastMovementAt = s.getLastMovementAt();
        e.updatedAt = s.getUpdatedAt();
        return e;
    }

    public void updateFromDomain(WarehouseStock s) {
        this.quantity = s.getQuantity();
        this.reserved = s.getReserved();
        this.lastMovementAt = s.getLastMovementAt();
        this.updatedAt = s.getUpdatedAt();
    }

    public WarehouseStock toDomain() {
        return new WarehouseStock(id, warehouseId, productId, quantity, reserved, lastMovementAt, updatedAt);
    }
}
