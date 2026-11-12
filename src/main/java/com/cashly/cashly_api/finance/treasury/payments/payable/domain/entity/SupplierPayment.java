package com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class SupplierPayment {

    private final SupplierPaymentId id;
    private final SupplierPaymentNumber paymentNumber;
    private final UUID supplierId;
    private final BigDecimal amount;
    private final UUID paymentMethodId;
    private final SupplierPaymentSource source;
    private final String referenceNumber;
    private final LocalDate paymentDate;
    private final String notes;
    private SupplierPaymentStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancelledReason;
    private UUID verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;

    private SupplierPayment(SupplierPaymentId id,
            SupplierPaymentNumber paymentNumber, UUID supplierId,
            BigDecimal amount, UUID paymentMethodId,
            SupplierPaymentSource source, String referenceNumber,
            LocalDate paymentDate, String notes, SupplierPaymentStatus status,
            BigDecimal appliedAmount, UUID createdBy, String createdByName,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            String cancelledReason, UUID verifiedBy, String verifiedByName,
            LocalDateTime verifiedAt) {
        this.id = id;
        this.paymentNumber = paymentNumber;
        this.supplierId = supplierId;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
        this.source = source;
        this.referenceNumber = referenceNumber;
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.status = status;
        this.appliedAmount = appliedAmount;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledReason = cancelledReason;
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.verifiedAt = verifiedAt;
    }

    public static SupplierPayment create(SupplierPaymentNumber paymentNumber,
            UUID supplierId, BigDecimal amount, UUID paymentMethodId,
            String referenceNumber, LocalDate paymentDate, String notes,
            UUID createdBy, String createdByName,
            SupplierPaymentStatus status) {
        Objects.requireNonNull(paymentNumber,
                "Payment number cannot be null");
        Objects.requireNonNull(supplierId, "Supplier id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
        Objects.requireNonNull(paymentDate, "Payment date cannot be null");
        Objects.requireNonNull(status, "Payment status cannot be null");
        LocalDateTime now = LocalDateTime.now();
        return new SupplierPayment(SupplierPaymentId.generate(), paymentNumber,
                supplierId, amount, paymentMethodId,
                SupplierPaymentSource.PAYMENT_METHOD, referenceNumber,
                paymentDate, notes, status, BigDecimal.ZERO, createdBy,
                createdByName, now, now, null, null, null, null);
    }

    public static SupplierPayment reconstitute(SupplierPaymentId id,
            SupplierPaymentNumber paymentNumber, UUID supplierId,
            BigDecimal amount, UUID paymentMethodId,
            SupplierPaymentSource source, String referenceNumber,
            LocalDate paymentDate, String notes, SupplierPaymentStatus status,
            BigDecimal appliedAmount, UUID createdBy, String createdByName,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            String cancelledReason, UUID verifiedBy, String verifiedByName,
            LocalDateTime verifiedAt) {
        return new SupplierPayment(id, paymentNumber, supplierId, amount,
                paymentMethodId, source, referenceNumber, paymentDate, notes,
                status, appliedAmount, createdBy, createdByName, createdAt,
                updatedAt, cancelledReason, verifiedBy, verifiedByName,
                verifiedAt);
    }

    public void apply(BigDecimal amountToApply) {
        if (this.status == SupplierPaymentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot apply to a cancelled payment");
        }
        Objects.requireNonNull(amountToApply, "Amount cannot be null");
        if (amountToApply.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
        BigDecimal newApplied = this.appliedAmount.add(amountToApply);
        if (newApplied.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException(
                    "Applied amount cannot exceed payment total");
        }
        this.appliedAmount = newApplied;
        this.status = SupplierPaymentStatus.APPLIED;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String reason, UUID userId, String userName) {
        if (this.status != SupplierPaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payments can be approved, current: "
                            + this.status);
        }
        Objects.requireNonNull(reason, "Approval reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Approval reason cannot be blank");
        }
        this.status = SupplierPaymentStatus.APPLIED;
        this.verifiedBy = userId;
        this.verifiedByName = userName;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason, UUID userId, String userName) {
        if (this.status != SupplierPaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payments can be rejected, current: "
                            + this.status);
        }
        Objects.requireNonNull(reason, "Rejection reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Rejection reason cannot be blank");
        }
        this.status = SupplierPaymentStatus.REJECTED;
        this.cancelledReason = reason;
        this.verifiedBy = userId;
        this.verifiedByName = userName;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == SupplierPaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        if (this.status == SupplierPaymentStatus.APPLIED) {
            throw new IllegalStateException(
                    "Cannot cancel an applied payment");
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot be blank");
        }
        this.status = SupplierPaymentStatus.CANCELLED;
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public SupplierPaymentId getId() {
        return id;
    }
    public SupplierPaymentNumber getPaymentNumber() {
        return paymentNumber;
    }
    public UUID getSupplierId() {
        return supplierId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }
    public SupplierPaymentSource getSource() {
        return source;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    public String getNotes() {
        return notes;
    }
    public SupplierPaymentStatus getStatus() {
        return status;
    }
    public BigDecimal getAppliedAmount() {
        return appliedAmount;
    }
    public UUID getCreatedBy() {
        return createdBy;
    }
    public String getCreatedByName() {
        return createdByName;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public String getCancelledReason() {
        return cancelledReason;
    }
    public UUID getVerifiedBy() {
        return verifiedBy;
    }
    public String getVerifiedByName() {
        return verifiedByName;
    }
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}
