package com.cashly.cashly_api.returns.domain.entities;

import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Return {

    private final ReturnId id;
    private final ReturnNumber returnNumber;
    private final InvoiceId invoiceId;
    private final UUID customerId;
    private final ReturnType returnType;
    private final String reason;
    private final BigDecimal total;
    private final List<ReturnLine> lines;
    private final List<CancelledAllocation> cancelledAllocations;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Return(ReturnId id, ReturnNumber returnNumber, InvoiceId invoiceId, UUID customerId,
                   ReturnType returnType, String reason, BigDecimal total,
                   List<ReturnLine> lines,
                   List<CancelledAllocation> cancelledAllocations,
                   UUID createdBy, String createdByName,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.returnNumber = returnNumber;
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.returnType = returnType;
        this.reason = reason;
        this.total = total;
        this.lines = new ArrayList<>(lines != null ? lines : List.of());
        this.cancelledAllocations = new ArrayList<>(cancelledAllocations != null ? cancelledAllocations : List.of());
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Return create(ReturnNumber returnNumber, InvoiceId invoiceId, UUID customerId,
                                ReturnType returnType, String reason, BigDecimal total,
                                List<ReturnLine> lines,
                                UUID createdBy, String createdByName) {
        Objects.requireNonNull(returnNumber, "Return number cannot be null");
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(returnType, "Return type cannot be null");
        Objects.requireNonNull(reason, "Reason cannot be null");
        Objects.requireNonNull(total, "Total cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");

        if (reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be blank");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Return must have at least one line");
        }

        BigDecimal calculatedTotal = lines.stream()
                .map(ReturnLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedTotal.compareTo(total) != 0) {
            throw new IllegalArgumentException("Total does not match sum of line subtotals");
        }

        LocalDateTime now = LocalDateTime.now();
        return new Return(ReturnId.generate(), returnNumber, invoiceId, customerId,
                returnType, reason, total,
                lines, List.of(), createdBy, createdByName, now, now);
    }

    public static Return reconstitute(ReturnId id, ReturnNumber returnNumber, InvoiceId invoiceId,
                                       UUID customerId,
                                       ReturnType returnType, String reason, BigDecimal total,
                                       List<ReturnLine> lines,
                                       List<CancelledAllocation> cancelledAllocations,
                                       UUID createdBy, String createdByName,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Return(id, returnNumber, invoiceId, customerId,
                returnType, reason, total,
                lines, cancelledAllocations, createdBy, createdByName, createdAt, updatedAt);
    }

    public ReturnId getId() { return id; }
    public ReturnNumber getReturnNumber() { return returnNumber; }
    public InvoiceId getInvoiceId() { return invoiceId; }
    public UUID getCustomerId() { return customerId; }
    public ReturnType getReturnType() { return returnType; }
    public String getReason() { return reason; }
    public BigDecimal getTotal() { return total; }
    public List<ReturnLine> getLines() { return Collections.unmodifiableList(lines); }
    public List<CancelledAllocation> getCancelledAllocations() { return Collections.unmodifiableList(cancelledAllocations); }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Return aReturn = (Return) o;
        return Objects.equals(id, aReturn.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Return{id=" + id + ", returnNumber=" + returnNumber + "}";
    }
}
