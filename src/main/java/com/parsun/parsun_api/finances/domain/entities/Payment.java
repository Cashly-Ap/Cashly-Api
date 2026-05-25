package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final PaymentId id;
    private final PaymentNumber paymentNumber;
    private final UUID customerId;
    private final PaymentType type;
    private final BigDecimal amount;
    private final UUID paymentMethodId;
    private final String referenceNumber;
    private final LocalDate paymentDate;
    private final String notes;
    private PaymentStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Payment(PaymentId id, PaymentNumber paymentNumber, UUID customerId, PaymentType type,
                    BigDecimal amount, UUID paymentMethodId, String referenceNumber, LocalDate paymentDate,
                    String notes, PaymentStatus status, BigDecimal appliedAmount,
                    UUID createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.paymentNumber = paymentNumber;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
        this.referenceNumber = referenceNumber;
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.status = status;
        this.appliedAmount = appliedAmount;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvalStatus = null;
        this.rejectionReason = null;
        this.approvedBy = null;
        this.commissionPercentage = null;
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId, PaymentType type,
                                 BigDecimal amount, UUID paymentMethodId, String referenceNumber,
                                 LocalDate paymentDate, String notes, UUID createdBy, String createdByName) {
        Objects.requireNonNull(paymentNumber, "Payment number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(type, "Payment type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Objects.requireNonNull(paymentMethodId, "Payment method cannot be null");
        Objects.requireNonNull(paymentDate, "Payment date cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new Payment(PaymentId.generate(), paymentNumber, customerId, type, amount,
                paymentMethodId, referenceNumber, paymentDate, notes,
                PaymentStatus.DRAFT, BigDecimal.ZERO, createdBy, createdByName, now, now);
    }

    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        PaymentType type, BigDecimal amount, UUID paymentMethodId,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Payment(id, paymentNumber, customerId, type, amount, paymentMethodId,
                referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt);
    }

    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        PaymentType type, BigDecimal amount, UUID paymentMethodId,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt,
                                        String approvalStatus, String rejectionReason,
                                        UUID approvedBy, BigDecimal commissionPercentage) {
        Payment payment = new Payment(id, paymentNumber, customerId, type, amount, paymentMethodId,
                referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt);
        payment.approvalStatus = approvalStatus;
        payment.rejectionReason = rejectionReason;
        payment.approvedBy = approvedBy;
        payment.commissionPercentage = commissionPercentage;
        return payment;
    }

    public void apply(BigDecimal amount) {
        if (this.status != PaymentStatus.DRAFT && this.status != PaymentStatus.PARTIALLY_APPLIED) {
            throw new IllegalStateException("Only DRAFT or PARTIALLY_APPLIED payments can be applied");
        }
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        BigDecimal newApplied = this.appliedAmount.add(amount);
        if (newApplied.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Applied amount cannot exceed payment total");
        }
        this.appliedAmount = newApplied;
        if (this.appliedAmount.compareTo(this.amount) == 0) {
            this.status = PaymentStatus.APPLIED;
        } else {
            this.status = PaymentStatus.PARTIALLY_APPLIED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    private String approvalStatus;
    private String rejectionReason;
    private UUID approvedBy;
    private BigDecimal commissionPercentage;

    public void submitForApproval(String voucherPath) {
        if (this.status != PaymentStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT payments can be submitted for approval");
        }
        this.status = PaymentStatus.DRAFT;
        this.approvalStatus = "PENDING_APPROVAL";
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(UUID authenticatedUserId, BigDecimal commissionPercentage) {
        if (!"PENDING_APPROVAL".equals(this.approvalStatus)) {
            throw new IllegalStateException("Payment is not pending approval");
        }
        this.approvalStatus = "APPROVED";
        this.approvedBy = authenticatedUserId;
        this.commissionPercentage = commissionPercentage;
        this.status = PaymentStatus.APPLIED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(UUID authenticatedUserId, String reason) {
        if (!"PENDING_APPROVAL".equals(this.approvalStatus)) {
            throw new IllegalStateException("Payment is not pending approval");
        }
        this.approvalStatus = "REJECTED";
        this.rejectionReason = reason;
        this.approvedBy = authenticatedUserId;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == PaymentStatus.DRAFT) {
            throw new IllegalStateException("DRAFT payments cannot be cancelled, delete them instead");
        }
        if (this.status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void unapply() {
        if (this.status != PaymentStatus.APPLIED && this.status != PaymentStatus.PARTIALLY_APPLIED) {
            throw new IllegalStateException("Only APPLIED or PARTIALLY_APPLIED payments can be unapplied");
        }
        this.appliedAmount = BigDecimal.ZERO;
        this.status = PaymentStatus.DRAFT;
        this.updatedAt = LocalDateTime.now();
    }

    public String getApprovalStatus() { return approvalStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public UUID getApprovedBy() { return approvedBy; }
    public BigDecimal getCommissionPercentage() { return commissionPercentage; }

    public PaymentId getId() { return id; }
    public PaymentNumber getPaymentNumber() { return paymentNumber; }
    public UUID getCustomerId() { return customerId; }
    public PaymentType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public String getReferenceNumber() { return referenceNumber; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getNotes() { return notes; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAppliedAmount() { return appliedAmount; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", paymentNumber=" + paymentNumber + ", type=" + type + ", amount=" + amount + ", status=" + status + "}";
    }
}
