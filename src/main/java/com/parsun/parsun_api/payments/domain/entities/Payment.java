package com.parsun.parsun_api.payments.domain.entities;

import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
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
    private final String referenceNumber;
    private final LocalDate paymentDate;
    private final String notes;
    private PaymentStatus status;
    private BigDecimal appliedAmount;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Payment(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                    BigDecimal amount, UUID paymentMethodId, String referenceNumber, LocalDate paymentDate,
                    String notes, PaymentStatus status, BigDecimal appliedAmount,
                    UUID createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.paymentNumber = paymentNumber;
        this.customerId = customerId;
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
    }

    public static Payment create(PaymentNumber paymentNumber, UUID customerId,
                                  BigDecimal amount, UUID paymentMethodId, String referenceNumber,
                                  LocalDate paymentDate, String notes, UUID createdBy, String createdByName,
                                  PaymentStatus status) {
        Objects.requireNonNull(paymentNumber, "Payment number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Objects.requireNonNull(paymentMethodId, "Payment method cannot be null");
        Objects.requireNonNull(paymentDate, "Payment date cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");
        Objects.requireNonNull(status, "Payment status cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new Payment(PaymentId.generate(), paymentNumber, customerId,
                amount, paymentMethodId, referenceNumber, paymentDate, notes,
                status, BigDecimal.ZERO, createdBy, createdByName, now, now);
    }

    public static Payment reconstitute(PaymentId id, PaymentNumber paymentNumber, UUID customerId,
                                        BigDecimal amount, UUID paymentMethodId,
                                        String referenceNumber, LocalDate paymentDate, String notes,
                                        PaymentStatus status, BigDecimal appliedAmount,
                                        UUID createdBy, String createdByName,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Payment(id, paymentNumber, customerId,
                amount, paymentMethodId, referenceNumber, paymentDate, notes, status, appliedAmount,
                createdBy, createdByName, createdAt, updatedAt);
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

    public void cancel() {
        if (this.status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentId getId() { return id; }
    public PaymentNumber getPaymentNumber() { return paymentNumber; }
    public UUID getCustomerId() { return customerId; }
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
        return "Payment{id=" + id + ", paymentNumber=" + paymentNumber + ", amount=" + amount + ", status=" + status + "}";
    }
}
