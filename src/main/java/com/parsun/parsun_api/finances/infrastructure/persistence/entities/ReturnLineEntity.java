package com.parsun.parsun_api.finances.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "return_lines")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReturnLineEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "return_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID returnId;

    @Column(name = "invoice_line_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID invoiceLineId;

    @Column(name = "product_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID productId;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "warehouse_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID warehouseId;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;
}
