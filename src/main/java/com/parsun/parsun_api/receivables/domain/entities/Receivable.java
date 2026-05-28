package com.parsun.parsun_api.receivables.domain.entities;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableId;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Receivable {

    private final ReceivableId id;
    private final InvoiceId invoiceId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private ReceivableStatus status;
    private final LocalDate dueDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Receivable(ReceivableId id, InvoiceId invoiceId, BigDecimal totalAmount,
                       BigDecimal paidAmount, ReceivableStatus status, LocalDate dueDate,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Receivable create(InvoiceId invoiceId, BigDecimal totalAmount,
                                    LocalDate dueDate) {
        Objects.requireNonNull(invoiceId, "InvoiceId cannot be null");
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }

        LocalDateTime now = LocalDateTime.now();
        return new Receivable(ReceivableId.generate(), invoiceId, totalAmount,
                BigDecimal.ZERO, ReceivableStatus.PENDING, dueDate, now, now);
    }

    public static Receivable reconstitute(ReceivableId id, InvoiceId invoiceId,
                                           BigDecimal totalAmount, BigDecimal paidAmount,
                                           ReceivableStatus status, LocalDate dueDate,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Receivable(id, invoiceId, totalAmount, paidAmount, status,
                dueDate, createdAt, updatedAt);
    }

    public void applyPayment(BigDecimal amount) {
        Objects.requireNonNull(amount, "Payment amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        this.paidAmount = this.paidAmount.add(amount);

        if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.status = ReceivableStatus.PAID;
        } else {
            this.status = ReceivableStatus.PARTIALLY_PAID;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markOverdue() {
        if (this.status == ReceivableStatus.PENDING || this.status == ReceivableStatus.PARTIALLY_PAID) {
            this.status = ReceivableStatus.OVERDUE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void writeOff() {
        if (this.status == ReceivableStatus.OVERDUE) {
            this.status = ReceivableStatus.WRITTEN_OFF;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public BigDecimal getBalance() {
        return totalAmount.subtract(paidAmount);
    }

    public ReceivableId getId() { return id; }
    public InvoiceId getInvoiceId() { return invoiceId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public ReceivableStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
