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
@Table(name = "supplier_balance_transactions")
@Getter
@Setter
@NoArgsConstructor
public class SupplierBalanceTransactionEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "supplier_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID supplierId;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "reference_type", length = 20, nullable = false)
    private String referenceType;

    @Column(name = "reference_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID referenceId;

    @Column(name = "number", length = 100)
    private String number;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
