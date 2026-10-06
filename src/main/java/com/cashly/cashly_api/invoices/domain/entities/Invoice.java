package com.cashly.cashly_api.invoices.domain.entities;

import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotReturnableException;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceReturnStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Invoice {

    private final InvoiceId id;
    private final InvoiceNumber invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerRif;
    private final UUID createdBy;
    private final String createdByName;
    private final UUID orderId;
    private final String orderNumber;
    private final List<UUID> orderIds;
    private final List<UUID> quoteIds;
    private InvoiceStatus status;
    private PaymentType paymentType;
    private String notes;
    private BigDecimal subtotal;
    private BigDecimal returnedTotal;
    private InvoiceReturnStatus returnStatus;
    private LocalDateTime cancelledAt;
    private String cancelledReason;
    private final List<InvoiceLine> lines;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate issueDate;
    private LocalDate dueDate;

    private Invoice(InvoiceId id, InvoiceNumber invoiceNumber, UUID customerId, String customerName,
                    String customerRif, UUID createdBy, String createdByName,
                    UUID orderId, String orderNumber,
                    List<UUID> orderIds, List<UUID> quoteIds,
                    InvoiceStatus status, PaymentType paymentType, String notes,
                    BigDecimal returnedTotal, InvoiceReturnStatus returnStatus,
                    LocalDateTime cancelledAt, String cancelledReason,
                    List<InvoiceLine> lines, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerRif = customerRif;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderIds = new ArrayList<>(orderIds != null ? orderIds : List.of());
        this.quoteIds = new ArrayList<>(quoteIds != null ? quoteIds : List.of());
        this.status = status;
        this.paymentType = paymentType;
        this.notes = notes;
        this.returnedTotal = returnedTotal != null ? returnedTotal : BigDecimal.ZERO;
        this.returnStatus = returnStatus;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
        this.lines = new ArrayList<>(lines != null ? lines : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.issueDate = createdAt.toLocalDate();
        this.dueDate = createdAt.toLocalDate();
        this.subtotal = calculateSubtotal();
    }

    public static Invoice create(InvoiceNumber invoiceNumber, UUID customerId, String customerName,
                                  String customerRif, UUID createdBy, String createdByName,
                                  UUID orderId, String orderNumber,
                                  PaymentType paymentType, String notes, List<InvoiceLine> lines) {
        return create(invoiceNumber, customerId, customerName, customerRif, createdBy, createdByName,
                orderId, orderNumber, List.of(), List.of(), paymentType, notes, lines);
    }

    public static Invoice create(InvoiceNumber invoiceNumber, UUID customerId, String customerName,
                                  String customerRif, UUID createdBy, String createdByName,
                                  UUID orderId, String orderNumber, List<UUID> orderIds,
                                  List<UUID> quoteIds, PaymentType paymentType, String notes,
                                  List<InvoiceLine> lines) {
        Objects.requireNonNull(invoiceNumber, "Invoice number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(customerName, "Customer name cannot be null");
        Objects.requireNonNull(customerRif, "Customer RIF cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");
        Objects.requireNonNull(paymentType, "Payment type cannot be null");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one line");
        }

        LocalDateTime now = LocalDateTime.now();
        return new Invoice(InvoiceId.generate(), invoiceNumber, customerId, customerName, customerRif,
                createdBy, createdByName, orderId, orderNumber,
                orderIds, quoteIds,
                InvoiceStatus.DRAFT, paymentType, notes,
                BigDecimal.ZERO, null,
                null, null, lines, now, now);
    }

    public static Invoice create(InvoiceNumber invoiceNumber, UUID customerId, String customerName,
                                  String customerRif, UUID createdBy, String createdByName,
                                  UUID orderId, String orderNumber, List<UUID> orderIds,
                                  List<UUID> quoteIds, PaymentType paymentType, String notes,
                                  List<InvoiceLine> lines, LocalDate issueDate, LocalDate dueDate) {
        Invoice invoice = create(invoiceNumber, customerId, customerName, customerRif, createdBy,
                createdByName, orderId, orderNumber, orderIds, quoteIds, paymentType, notes, lines);
        invoice.setDates(issueDate, dueDate);
        return invoice;
    }

    public static Invoice reconstitute(InvoiceId id, InvoiceNumber invoiceNumber, UUID customerId,
                                        String customerName, String customerRif,
                                        UUID createdBy, String createdByName,
                                        UUID orderId, String orderNumber,
                                        InvoiceStatus status, PaymentType paymentType, String notes,
                                        BigDecimal returnedTotal, InvoiceReturnStatus returnStatus,
                                        LocalDateTime cancelledAt, String cancelledReason,
                                        List<InvoiceLine> lines, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        return reconstitute(id, invoiceNumber, customerId, customerName, customerRif,
                createdBy, createdByName, orderId, orderNumber, List.of(), List.of(), status,
                paymentType, notes, returnedTotal, returnStatus, cancelledAt, cancelledReason,
                lines, createdAt, updatedAt);
    }

    public static Invoice reconstitute(InvoiceId id, InvoiceNumber invoiceNumber, UUID customerId,
                                        String customerName, String customerRif,
                                        UUID createdBy, String createdByName,
                                        UUID orderId, String orderNumber, List<UUID> orderIds,
                                        List<UUID> quoteIds, InvoiceStatus status, PaymentType paymentType,
                                        String notes, BigDecimal returnedTotal, InvoiceReturnStatus returnStatus,
                                        LocalDateTime cancelledAt, String cancelledReason,
                                        List<InvoiceLine> lines, LocalDateTime createdAt,
                                        LocalDateTime updatedAt, LocalDate issueDate, LocalDate dueDate) {
        Invoice invoice = reconstitute(id, invoiceNumber, customerId, customerName, customerRif,
                createdBy, createdByName, orderId, orderNumber, orderIds, quoteIds, status,
                paymentType, notes, returnedTotal, returnStatus, cancelledAt, cancelledReason,
                lines, createdAt, updatedAt);
        invoice.setDates(issueDate, dueDate);
        return invoice;
    }

    private void setDates(LocalDate issueDate, LocalDate dueDate) {
        Objects.requireNonNull(issueDate, "Issue date cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date");
        }
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public void updateDates(LocalDate issueDate, LocalDate dueDate) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be updated");
        }
        setDates(issueDate, dueDate);
        this.updatedAt = LocalDateTime.now();
    }

    public static Invoice reconstitute(InvoiceId id, InvoiceNumber invoiceNumber, UUID customerId,
                                        String customerName, String customerRif,
                                        UUID createdBy, String createdByName,
                                        UUID orderId, String orderNumber, List<UUID> orderIds,
                                        List<UUID> quoteIds, InvoiceStatus status, PaymentType paymentType, String notes,
                                        BigDecimal returnedTotal, InvoiceReturnStatus returnStatus,
                                        LocalDateTime cancelledAt, String cancelledReason,
                                        List<InvoiceLine> lines, LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        return new Invoice(id, invoiceNumber, customerId, customerName, customerRif,
                createdBy, createdByName, orderId, orderNumber,
                orderIds, quoteIds,
                status, paymentType, notes,
                returnedTotal, returnStatus,
                cancelledAt, cancelledReason,
                lines, createdAt, updatedAt);
    }

    public void update(PaymentType paymentType, String notes, List<InvoiceLine> newLines) {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be updated");
        }
        Objects.requireNonNull(paymentType, "Payment type cannot be null");
        if (newLines == null || newLines.isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one line");
        }
        this.paymentType = paymentType;
        this.notes = notes;
        this.lines.clear();
        this.lines.addAll(newLines);
        this.subtotal = calculateSubtotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be confirmed");
        }
        if (this.paymentType == PaymentType.CASH) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PENDING_PAYMENT;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be deleted");
        }
    }

    public void cancel(String reason) {
        if (this.status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled");
        }
        if (this.status == InvoiceStatus.FULLY_RETURNED) {
            throw new IllegalStateException("Cannot cancel an invoice that is FULLY_RETURNED");
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }
        this.status = InvoiceStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void recalculatePaymentStatus(BigDecimal totalPaid, BigDecimal totalDebitNotes, BigDecimal totalCreditNotes) {
        if (this.status == InvoiceStatus.DRAFT
                || this.status == InvoiceStatus.CANCELLED
                || this.status == InvoiceStatus.FULLY_RETURNED) {
            return;
        }

        BigDecimal effectiveTotal = this.calculateTotal(
                totalCreditNotes != null ? totalCreditNotes : BigDecimal.ZERO,
                totalDebitNotes != null ? totalDebitNotes : BigDecimal.ZERO
        );
        BigDecimal balance = effectiveTotal
                .subtract(totalPaid != null ? totalPaid : BigDecimal.ZERO);

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InvoiceStatus.PAID;
        } else if (balance.compareTo(effectiveTotal) < 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        } else {
            this.status = InvoiceStatus.PENDING_PAYMENT;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void applyReturn(Return return_) {
        Objects.requireNonNull(return_, "Return cannot be null");

        if (this.status == InvoiceStatus.FULLY_RETURNED || this.status == InvoiceStatus.CANCELLED) {
            throw new InvoiceNotReturnableException(this.id, this.status);
        }

        if (!return_.getInvoiceId().value().equals(this.id.value())) {
            throw new IllegalArgumentException("La devolución no pertenece a esta factura");
        }

        this.returnedTotal = this.returnedTotal.add(return_.getTotal());

        for (ReturnLine returnLine : return_.getLines()) {
            this.lines.stream()
                    .filter(l -> l.getId().value().equals(returnLine.getInvoiceLineId()))
                    .findFirst()
                    .ifPresent(line -> line.accumulateReturnedQuantity(returnLine.getQuantity()));
        }

        boolean allFullyReturned = this.lines.stream()
                .allMatch(line -> line.getReturnedQuantity().compareTo(line.getQuantity()) >= 0);

        this.returnStatus = allFullyReturned ? InvoiceReturnStatus.FULL : InvoiceReturnStatus.PARTIAL;

        if (return_.getReturnType() == ReturnType.AUTOMATIC || allFullyReturned) {
            this.status = InvoiceStatus.FULLY_RETURNED;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void revertReturn(Return return_) {
        Objects.requireNonNull(return_, "Return cannot be null");

        if (!return_.getInvoiceId().value().equals(this.id.value())) {
            throw new IllegalArgumentException("La devolución no pertenece a esta factura");
        }

        this.returnedTotal = this.returnedTotal.subtract(return_.getTotal());

        for (ReturnLine returnLine : return_.getLines()) {
            this.lines.stream()
                    .filter(l -> l.getId().value().equals(returnLine.getInvoiceLineId()))
                    .findFirst()
                    .ifPresent(line -> line.decreaseReturnedQuantity(returnLine.getQuantity()));
        }

        boolean anyReturned = this.lines.stream()
                .anyMatch(line -> line.getReturnedQuantity().compareTo(BigDecimal.ZERO) > 0);
        boolean allFullyReturned = this.lines.stream()
                .allMatch(line -> line.getReturnedQuantity().compareTo(line.getQuantity()) >= 0);

        if (!anyReturned) {
            this.returnStatus = null;
        } else if (allFullyReturned) {
            this.returnStatus = InvoiceReturnStatus.FULL;
        } else {
            this.returnStatus = InvoiceReturnStatus.PARTIAL;
        }

        this.updatedAt = LocalDateTime.now();
    }

    private BigDecimal calculateSubtotal() {
        return lines.stream()
                .map(InvoiceLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotal(BigDecimal creditApplied, BigDecimal debitApplied) {
        if (subtotal == null) {
            return BigDecimal.ZERO;
        }
        return subtotal.subtract(returnedTotal)
                .subtract(creditApplied)
                .add(debitApplied);
    }

    public BigDecimal calculateTotal() {
        return calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public InvoiceId getId() { return id; }
    public InvoiceNumber getInvoiceNumber() { return invoiceNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerRif() { return customerRif; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public UUID getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public List<UUID> getOrderIds() { return Collections.unmodifiableList(orderIds); }
    public List<UUID> getQuoteIds() { return Collections.unmodifiableList(quoteIds); }
    public InvoiceStatus getStatus() { return status; }
    public PaymentType getPaymentType() { return paymentType; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getReturnedTotal() { return returnedTotal; }
    public InvoiceReturnStatus getReturnStatus() { return returnStatus; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancelledReason() { return cancelledReason; }
    public List<InvoiceLine> getLines() { return Collections.unmodifiableList(lines); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
}
