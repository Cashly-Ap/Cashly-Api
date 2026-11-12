package com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "purchase_number", length = 20, nullable = false, unique = true)
    private String purchaseNumber;

    @Column(name = "supplier_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID supplierId;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "supplier_rif", length = 50)
    private String supplierRif;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID createdBy;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "payment_type", length = 20)
    private String paymentType;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "returned_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal returnedTotal;

    @Column(name = "return_status", length = 20)
    private String returnStatus;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseLineEntity> lines = new ArrayList<>();
}
