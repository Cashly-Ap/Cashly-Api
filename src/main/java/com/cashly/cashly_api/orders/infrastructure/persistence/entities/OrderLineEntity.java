package com.cashly.cashly_api.orders.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderLineEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private OrderEntity order;

    @Column(name = "product_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID productId;

    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    @OneToOne(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderLineCategorySnapshotEntity categorySnapshot;

    @OneToMany(mappedBy = "orderLine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLineAllocationEntity> allocations = new ArrayList<>();

    public void setCategorySnapshot(UUID categoryId) {
        categorySnapshot = categoryId == null ? null : new OrderLineCategorySnapshotEntity(this, categoryId);
    }

    public UUID getCategoryIdSnapshot() {
        return categorySnapshot == null ? null : categorySnapshot.getCategoryId();
    }
}
