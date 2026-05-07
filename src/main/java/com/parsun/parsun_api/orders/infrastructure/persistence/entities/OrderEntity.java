package com.parsun.parsun_api.orders.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "order_number", length = 20, nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID customerId;

    @Column(name = "customer_name", length = 200, nullable = false)
    private String customerName;

    @Column(name = "customer_rif", length = 50, nullable = false)
    private String customerRif;

    @Column(name = "created_by", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID createdBy;

    @Column(name = "created_by_name", length = 200, nullable = false)
    private String createdByName;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "invoice_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID invoiceId;

    @Column(name = "invoice_number", length = 20)
    private String invoiceNumber;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLineEntity> lines = new ArrayList<>();
}
