package com.cashly.cashly_api.procurement.purchases.domain.entity;

import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Purchase {

    private final PurchaseId id;
    private final PurchaseNumber purchaseNumber;
    private final UUID supplierId;
    private final String supplierName;
    private final String supplierRif;
    private final UUID createdBy;
    private final String createdByName;
    private PurchaseStatus status;
    private PaymentType paymentType;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal returnedTotal;
    private String returnStatus;
    private String notes;
    private BigDecimal subtotal;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancelledReason;
    private final List<PurchaseLine> lines;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Purchase(PurchaseId id, PurchaseNumber purchaseNumber,
            UUID supplierId, String supplierName, String supplierRif,
            UUID createdBy, String createdByName, PurchaseStatus status,
            PaymentType paymentType, LocalDate issueDate, LocalDate dueDate,
            BigDecimal returnedTotal, String returnStatus, String notes,
            BigDecimal subtotal, LocalDateTime confirmedAt,
            LocalDateTime cancelledAt, String cancelledReason,
            List<PurchaseLine> lines, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.purchaseNumber = purchaseNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierRif = supplierRif;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.status = status;
        this.paymentType = paymentType;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnedTotal = returnedTotal != null ? returnedTotal
                : BigDecimal.ZERO;
        this.returnStatus = returnStatus;
        this.notes = notes;
        this.subtotal = subtotal;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
        this.lines = new ArrayList<>(lines != null ? lines : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Purchase create(PurchaseNumber purchaseNumber,
            UUID supplierId, String supplierName, String supplierRif,
            UUID createdBy, String createdByName, String notes,
            List<PurchaseLine> lines) {
        LocalDate today = LocalDate.now();
        return create(purchaseNumber, supplierId, supplierName, supplierRif,
                createdBy, createdByName, PaymentType.CREDIT, notes, lines,
                today, today.plusDays(30));
    }

    public static Purchase create(PurchaseNumber purchaseNumber,
            UUID supplierId, String supplierName, String supplierRif,
            UUID createdBy, String createdByName, PaymentType paymentType,
            String notes, List<PurchaseLine> lines, LocalDate issueDate,
            LocalDate dueDate) {
        Objects.requireNonNull(purchaseNumber,
                "Purchase number cannot be null");
        Objects.requireNonNull(supplierId, "Supplier id cannot be null");
        Objects.requireNonNull(supplierName, "Supplier name cannot be null");
        Objects.requireNonNull(supplierRif, "Supplier RIF cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");
        Objects.requireNonNull(paymentType, "Payment type cannot be null");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must have at least one line");
        }

        LocalDate issue = issueDate != null ? issueDate : LocalDate.now();
        LocalDate due = dueDate != null ? dueDate : issue.plusDays(30);
        if (due.isBefore(issue)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before issue date");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal calculatedSubtotal = calculateSubtotal(lines);

        return new Purchase(PurchaseId.generate(), purchaseNumber, supplierId,
                supplierName, supplierRif, createdBy, createdByName,
                PurchaseStatus.DRAFT, paymentType, issue, due, BigDecimal.ZERO,
                null, notes, calculatedSubtotal, null, null, null, lines, now,
                now);
    }

    public static Purchase reconstitute(PurchaseId id,
            PurchaseNumber purchaseNumber, UUID supplierId, String supplierName,
            String supplierRif, UUID createdBy, String createdByName,
            PurchaseStatus status, PaymentType paymentType, LocalDate issueDate,
            LocalDate dueDate, BigDecimal returnedTotal, String returnStatus,
            String notes, BigDecimal subtotal, LocalDateTime confirmedAt,
            LocalDateTime cancelledAt, String cancelledReason,
            List<PurchaseLine> lines, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new Purchase(id, purchaseNumber, supplierId, supplierName,
                supplierRif, createdBy, createdByName, status, paymentType,
                issueDate, dueDate, returnedTotal, returnStatus, notes, subtotal,
                confirmedAt, cancelledAt, cancelledReason, lines, createdAt,
                updatedAt);
    }

    public static Purchase reconstitute(PurchaseId id,
            PurchaseNumber purchaseNumber, UUID supplierId, String supplierName,
            String supplierRif, UUID createdBy, String createdByName,
            PurchaseStatus status, String notes, BigDecimal subtotal,
            LocalDateTime confirmedAt, LocalDateTime cancelledAt,
            String cancelledReason, List<PurchaseLine> lines,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return reconstitute(id, purchaseNumber, supplierId, supplierName,
                supplierRif, createdBy, createdByName, status,
                PaymentType.CREDIT, null, null, BigDecimal.ZERO, null, notes,
                subtotal, confirmedAt, cancelledAt, cancelledReason, lines,
                createdAt, updatedAt);
    }

    public void update(String notes, List<PurchaseLine> lines) {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT purchases can be updated");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must have at least one line");
        }
        this.notes = notes;
        this.lines.clear();
        this.lines.addAll(lines);
        this.subtotal = calculateSubtotal(this.lines);
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePaymentTerms(PaymentType paymentType, LocalDate issueDate,
            LocalDate dueDate) {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT purchases can change payment terms");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type cannot be null");
        }
        LocalDate issue = issueDate != null ? issueDate : LocalDate.now();
        LocalDate due = dueDate != null ? dueDate : issue.plusDays(30);
        if (due.isBefore(issue)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before issue date");
        }
        this.paymentType = paymentType;
        this.issueDate = issue;
        this.dueDate = due;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT purchases can be confirmed");
        }
        this.status = this.paymentType == PaymentType.CASH
                ? PurchaseStatus.PAID
                : PurchaseStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void applyPayment(BigDecimal amountPaid) {
        Objects.requireNonNull(amountPaid, "Paid amount cannot be null");
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal total = getBalanceTotal();
        this.status = amountPaid.compareTo(total) >= 0
                ? PurchaseStatus.PAID
                : PurchaseStatus.PARTIALLY_PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getBalanceTotal() {
        BigDecimal returned = returnedTotal != null ? returnedTotal
                : BigDecimal.ZERO;
        return subtotal.subtract(returned).max(BigDecimal.ZERO);
    }

    public void registerReturn(BigDecimal amount) {
        Objects.requireNonNull(amount, "Return amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Return amount must be greater than zero");
        }
        BigDecimal newReturned = getReturnedTotal().add(amount);
        if (newReturned.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Returned total cannot exceed purchase subtotal");
        }
        this.returnedTotal = newReturned;
        this.returnStatus = newReturned.compareTo(subtotal) >= 0
                ? "FULLY_RETURNED"
                : "PARTIAL_RETURN";
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status != PurchaseStatus.DRAFT
                && this.status != PurchaseStatus.CONFIRMED
                && this.status != PurchaseStatus.PARTIALLY_PAID) {
            throw new IllegalStateException(
                    "Only DRAFT or CONFIRMED purchases can be cancelled, current: "
                            + this.status);
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot be blank");
        }
        this.status = PurchaseStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT purchases can be deleted");
        }
    }

    private static BigDecimal calculateSubtotal(List<PurchaseLine> lines) {
        return lines.stream().map(PurchaseLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PurchaseId getId() {
        return id;
    }
    public PurchaseNumber getPurchaseNumber() {
        return purchaseNumber;
    }
    public UUID getSupplierId() {
        return supplierId;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public String getSupplierRif() {
        return supplierRif;
    }
    public UUID getCreatedBy() {
        return createdBy;
    }
    public String getCreatedByName() {
        return createdByName;
    }
    public PurchaseStatus getStatus() {
        return status;
    }
    public PaymentType getPaymentType() {
        return paymentType;
    }
    public LocalDate getIssueDate() {
        return issueDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public BigDecimal getReturnedTotal() {
        return returnedTotal != null ? returnedTotal : BigDecimal.ZERO;
    }
    public String getReturnStatus() {
        return returnStatus;
    }
    public String getNotes() {
        return notes;
    }
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    public String getCancelledReason() {
        return cancelledReason;
    }
    public List<PurchaseLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
