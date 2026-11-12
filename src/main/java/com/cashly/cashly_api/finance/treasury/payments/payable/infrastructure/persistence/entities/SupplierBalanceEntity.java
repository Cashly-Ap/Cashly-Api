package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "supplier_balances")
@Getter
@Setter
@NoArgsConstructor
public class SupplierBalanceEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "supplier_id", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID supplierId;

    @Column(name = "total_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalBalance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
