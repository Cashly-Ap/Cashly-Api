package com.parsun.parsun_api.returns.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "return_cancelled_allocations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReturnCancelledAllocationEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false, columnDefinition = "BINARY(16)")
    private ReturnEntity returnEntity;

    @Column(name = "payment_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID paymentId;

    @Column(name = "invoice_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID invoiceId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
}
