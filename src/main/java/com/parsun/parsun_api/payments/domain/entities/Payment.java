package com.parsun.parsun_api.payments.domain.entities;

import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentSource;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final PaymentId id;
    private final PaymentNumber paymentNumber;
    private final UUID customerId;
    private final BigDecimal amount;
    private final UUID paymentMethodId;
    private final PaymentSource source;
    private final String referenceNumber;
    private final LocalDate paymentDate;
    private final String notes;
    private PaymentStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancelledReason;
    private UUID verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private final UUID targetInvoiceId;
    private final String voucherPath;

    private Payment(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                    BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                    String referenceNumber, LocalDate paymentDate,
                    String notes, PaymentStatus status, BigDecimal appliedAmount,
                    UUID createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String cancelledReason, UUID verifiedBy, String verifiedByName, LocalDateTime verifiedAt,
                    UUID targetInvoiceId, String voucherPath) {
        this.id = id;
        this.paymentNumber = paymentNumber;
        this.customerId = customerId;
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
        this.targetInvoiceId = targetInvoiceId;
        this.voucherPath = voucherPath;
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId,
                                  BigDecimal amount, UUID paymentMethodId, String referenceNumber,
                                  LocalDate paymentDate, String notes, UUID createdBy, String createdByName,
                                  PaymentStatus status) {
        return create(paymentNumber, customerId, amount, paymentMethodId, referenceNumber,
                paymentDate, notes, createdBy, createdByName, status, null, null);
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId,
                                  BigDecimal amount, UUID paymentMethodId, String referenceNumber,
                                  LocalDate paymentDate, String notes, UUID createdBy, String createdByName,
                                  PaymentStatus status, UUID targetInvoiceId, String voucherPath) {
        return create(paymentNumber, customerId, amount, paymentMethodId, PaymentSource.PAYMENT_METHOD,
                referenceNumber, paymentDate, notes, createdBy, createdByName, status,
                targetInvoiceId, voucherPath);
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId,
                                  BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                                  String referenceNumber, LocalDate paymentDate, String notes,
                                  UUID createdBy, String createdByName, PaymentStatus status) {
        return create(paymentNumber, customerId, amount, paymentMethodId, source,
                referenceNumber, paymentDate, notes, createdBy, createdByName, status, null, null);
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId,
                                  BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                                  String referenceNumber, LocalDate paymentDate, String notes,
                                  UUID createdBy, String createdByName, PaymentStatus status,
                                  UUID targetInvoiceId, String voucherPath) {
        Objects.requireNonNull(paymentNumber, "Payment number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Objects.requireNonNull(source, "Payment source cannot be null");
        if (source == PaymentSource.PAYMENT_METHOD) {
            Objects.requireNonNull(paymentMethodId, "Payment method cannot be null when source is PAYMENT_METHOD");
        }
        Objects.requireNonNull(paymentDate, "Payment date cannot be null");
        Objects.requireNonNull(status, "Payment status cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new Payment(PaymentId.generate(), paymentNumber, customerId,
                amount, paymentMethodId, source, referenceNumber, paymentDate, notes,
                status, BigDecimal.ZERO, createdBy, createdByName, now, now,
                null, null, null, null, targetInvoiceId, voucherPath);
    }

    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt,
                                        String cancelledReason, UUID verifiedBy,
                                        String verifiedByName, LocalDateTime verifiedAt,
                                        UUID targetInvoiceId, String voucherPath) {
        return new Payment(id, paymentNumber, customerId,
                amount, paymentMethodId, source, referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt,
                cancelledReason, verifiedBy, verifiedByName, verifiedAt,
                targetInvoiceId, voucherPath);
    }

    @Deprecated
    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt,
                                        String cancelledReason, UUID verifiedBy,
                                        String verifiedByName, LocalDateTime verifiedAt) {
        return reconstitute(id, paymentNumber, customerId,
                amount, paymentMethodId, source, referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt,
                cancelledReason, verifiedBy, verifiedByName, verifiedAt, null, null);
    }

    @Deprecated
    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        BigDecimal amount, UUID paymentMethodId, PaymentSource source,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return reconstitute(id, paymentNumber, customerId,
                amount, paymentMethodId, source, referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt,
                null, null, null, null);
    }

    public void apply(BigDecimal amount) {
        if (this.status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply to a cancelled payment");
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
        this.status = PaymentStatus.APPLIED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        if (this.status == PaymentStatus.APPLIED) {
            throw new IllegalStateException("Cannot cancel an applied payment");
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    @Deprecated
    public void cancel() {
        if (this.status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String reason, UUID verifiedByUserId, String verifiedByUserName) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be approved, current: " + this.status);
        }
        Objects.requireNonNull(reason, "Approval reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Approval reason cannot be blank");
        }
        Objects.requireNonNull(verifiedByUserId, "Verified by user id cannot be null");
        Objects.requireNonNull(verifiedByUserName, "Verified by user name cannot be null");
        this.status = PaymentStatus.APPLIED;
        this.verifiedBy = verifiedByUserId;
        this.verifiedByName = verifiedByUserName;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason, UUID verifiedByUserId, String verifiedByUserName) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be rejected, current: " + this.status);
        }
        Objects.requireNonNull(reason, "Rejection reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be blank");
        }
        Objects.requireNonNull(verifiedByUserId, "Verified by user id cannot be null");
        Objects.requireNonNull(verifiedByUserName, "Verified by user name cannot be null");
        this.status = PaymentStatus.REJECTED;
        this.cancelledReason = reason;
        this.verifiedBy = verifiedByUserId;
        this.verifiedByName = verifiedByUserName;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        if (this.status != PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Only cancelled payments can be reactivated");
        }
        this.status = PaymentStatus.APPLIED;
        this.updatedAt = LocalDateTime.now();
    }

    public Payment withUpdatedDetails(String referenceNumber, LocalDate paymentDate, UUID paymentMethodId, String notes) {
        return reconstitute(
                this.id, this.paymentNumber, this.customerId,
                this.amount, paymentMethodId, this.source,
                referenceNumber, paymentDate, notes,
                this.status, this.appliedAmount,
                this.createdBy, this.createdByName,
                this.createdAt, LocalDateTime.now(),
                this.cancelledReason, this.verifiedBy, this.verifiedByName, this.verifiedAt,
                this.targetInvoiceId, this.voucherPath
        );
    }

    public PaymentId getId() {
        return id;
    }

    public PaymentNumber getPaymentNumber() {
        return paymentNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    public PaymentSource getSource() {
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

    public PaymentStatus getStatus() {
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

    public UUID getTargetInvoiceId() {
        return targetInvoiceId;
    }

    public String getVoucherPath() {
        return voucherPath;
    }

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
        return "Payment{id=" + id + ", paymentNumber=" + paymentNumber + ", amount=" + amount + ", status=" + status + "}";
    }
}
