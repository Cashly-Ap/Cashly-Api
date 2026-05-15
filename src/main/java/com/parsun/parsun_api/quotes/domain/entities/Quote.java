package com.parsun.parsun_api.quotes.domain.entities;

import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteLineId;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Quote {

    private final UUID id;
    private final QuoteNumber quoteNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerRif;
    private final UUID createdBy;
    private final String createdByName;
    private QuoteStatus status;
    private final LocalDate expiryDate;
    private String notes;
    private BigDecimal subtotal;
    private UUID orderId;
    private String orderNumber;
    private UUID invoiceId;
    private String invoiceNumber;
    private Instant cancelledAt;
    private String cancelledReason;
    private final List<QuoteLine> lines;
    private final Instant createdAt;
    private Instant updatedAt;

    // Private constructor — used by both create() and reconstitute()
    private Quote(UUID id, QuoteNumber quoteNumber, UUID customerId, String customerName,
                  String customerRif, UUID createdBy, String createdByName, QuoteStatus status,
                  LocalDate expiryDate, String notes, BigDecimal subtotal,
                  UUID orderId, String orderNumber, UUID invoiceId, String invoiceNumber,
                  Instant cancelledAt, String cancelledReason,
                  List<QuoteLine> lines, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.quoteNumber = quoteNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerRif = customerRif;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.status = status;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.subtotal = subtotal;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
        this.lines = new ArrayList<>(lines);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method — creates a new Quote in DRAFT status with validation.
     */
    public static Quote create(QuoteNumber quoteNumber, UUID customerId, String customerName,
                               String customerRif, UUID createdBy, String createdByName,
                               LocalDate expiryDate, String notes, List<QuoteLine> lines) {
        if (quoteNumber == null) {
            throw new IllegalArgumentException("quoteNumber must not be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (customerName == null) {
            throw new IllegalArgumentException("customerName must not be null");
        }
        if (customerRif == null) {
            throw new IllegalArgumentException("customerRif must not be null");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("createdBy must not be null");
        }
        if (createdByName == null) {
            throw new IllegalArgumentException("createdByName must not be null");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Quote must have at least one line");
        }

        Instant now = Instant.now();
        BigDecimal calculatedSubtotal = calculateSubtotal(lines);

        return new Quote(
                UUID.randomUUID(), quoteNumber, customerId, customerName, customerRif,
                createdBy, createdByName, QuoteStatus.DRAFT,
                expiryDate, notes, calculatedSubtotal,
                null, null, null, null,
                null, null,
                lines, now, now
        );
    }

    /**
     * Reconstitution factory — used by JPA mappers. NO validation.
     */
    public static Quote reconstitute(UUID id, QuoteNumber quoteNumber, UUID customerId,
                                     String customerName, String customerRif,
                                     UUID createdBy, String createdByName, QuoteStatus status,
                                     LocalDate expiryDate, String notes, BigDecimal subtotal,
                                     UUID orderId, String orderNumber, UUID invoiceId,
                                     String invoiceNumber, Instant cancelledAt,
                                     String cancelledReason, List<QuoteLine> lines,
                                     Instant createdAt, Instant updatedAt) {
        return new Quote(id, quoteNumber, customerId, customerName, customerRif,
                createdBy, createdByName, status,
                expiryDate, notes, subtotal,
                orderId, orderNumber, invoiceId, invoiceNumber,
                cancelledAt, cancelledReason,
                lines, createdAt, updatedAt);
    }

    // --- Status Transitions ---

    public void cancel(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason must not be null or blank");
        }
        assertCanTransition(QuoteStatus.CANCELLED);
        this.status = QuoteStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.cancelledReason = reason;
        touch();
    }

    public void markConvertedToOrder(UUID orderId, String orderNumber) {
        assertCanTransition(QuoteStatus.CONVERTED_TO_ORDER);
        this.status = QuoteStatus.CONVERTED_TO_ORDER;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        touch();
    }

    public void markConvertedToInvoice(UUID invoiceId, String invoiceNumber) {
        assertCanTransition(QuoteStatus.CONVERTED_TO_INVOICE);
        this.status = QuoteStatus.CONVERTED_TO_INVOICE;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        touch();
    }

    private void assertCanTransition(QuoteStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new QuoteStateException(
                    "Cannot transition from " + this.status + " to " + target);
        }
    }

    // --- Line Management (only in DRAFT) ---

    public void addLine(QuoteLine line) {
        assertDraft();
        lines.add(line);
        recalculateSubtotal();
        touch();
    }

    public void removeLine(QuoteLineId lineId) {
        assertDraft();
        lines.removeIf(l -> l.getId().equals(lineId));
        recalculateSubtotal();
        touch();
    }

    public void updateLine(QuoteLineId lineId, BigDecimal quantity, BigDecimal unitPrice) {
        assertDraft();
        for (QuoteLine line : lines) {
            if (line.getId().equals(lineId)) {
                line.updateQuantity(quantity);
                line.updateUnitPrice(unitPrice);
                break;
            }
        }
        recalculateSubtotal();
        touch();
    }

    private void assertDraft() {
        if (this.status != QuoteStatus.DRAFT) {
            throw new QuoteStateException("Quote can only be modified in DRAFT status");
        }
    }

    // --- Helpers ---

    private void recalculateSubtotal() {
        this.subtotal = calculateSubtotal(lines);
    }

    private static BigDecimal calculateSubtotal(List<QuoteLine> lines) {
        return lines.stream()
                .map(QuoteLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    // --- Guard Methods ---

    public boolean canBeModified() {
        return this.status == QuoteStatus.DRAFT;
    }

    public boolean canBeDeleted() {
        return this.status == QuoteStatus.DRAFT;
    }

    public boolean isActive() {
        return this.status != QuoteStatus.CANCELLED;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public QuoteNumber getQuoteNumber() { return quoteNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerRif() { return customerRif; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public QuoteStatus getStatus() { return status; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public UUID getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancelledReason() { return cancelledReason; }
    public List<QuoteLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
