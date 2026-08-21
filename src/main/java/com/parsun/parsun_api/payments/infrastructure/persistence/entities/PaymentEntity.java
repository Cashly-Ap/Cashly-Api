package com.parsun.parsun_api.payments.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "payment_number", length = 20, nullable = false, unique = true)
    private String paymentNumber;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID customerId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID paymentMethodId;

    @Column(name = "source", length = 20, nullable = false)
    private String source;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "applied_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal appliedAmount;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID createdBy;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @Column(name = "verified_by", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID verifiedBy;

    @Column(name = "verified_by_name", length = 200)
    private String verifiedByName;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "target_invoice_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID targetInvoiceId;

    @Column(name = "voucher_path", length = 500)
    private String voucherPath;
}
