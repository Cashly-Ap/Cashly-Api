package com.parsun.parsun_api.finances.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "debit_notes")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DebitNoteEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "note_number", length = 30, nullable = false, unique = true)
    private String noteNumber;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID customerId;

    @Column(name = "invoice_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID invoiceId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "applied_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal appliedAmount;

    @Column(name = "created_by", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID createdBy;

    @Column(name = "created_by_name", length = 200, nullable = false)
    private String createdByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
