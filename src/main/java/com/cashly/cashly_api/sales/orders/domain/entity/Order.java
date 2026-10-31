package com.cashly.cashly_api.sales.orders.domain.entity;

import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private final OrderId id;
    private final OrderNumber orderNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerRif;
    private final UUID createdBy;
    private final String createdByName;
    private OrderStatus status;
    private String notes;
    private BigDecimal subtotal;
    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDateTime cancelledAt;
    private String cancelledReason;
    private final List<OrderLine> lines;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Order(OrderId id, OrderNumber orderNumber, UUID customerId,
            String customerName, String customerRif, UUID createdBy,
            String createdByName, OrderStatus status, String notes,
            UUID invoiceId, String invoiceNumber, LocalDateTime cancelledAt,
            String cancelledReason, List<OrderLine> lines,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerRif = customerRif;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.status = status;
        this.notes = notes;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
        this.lines = new ArrayList<>(lines != null ? lines : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.subtotal = calculateSubtotal();
    }

    public static Order create(OrderNumber orderNumber, UUID customerId,
            String customerName, String customerRif, UUID createdBy,
            String createdByName, String notes, List<OrderLine> lines) {
        return createWithStatus(orderNumber, customerId, customerName,
                customerRif, createdBy, createdByName, notes, lines,
                OrderStatus.DRAFT);
    }

    private static Order createWithStatus(OrderNumber orderNumber,
            UUID customerId, String customerName, String customerRif,
            UUID createdBy, String createdByName, String notes,
            List<OrderLine> lines, OrderStatus status) {
        Objects.requireNonNull(orderNumber, "Order number cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(customerName, "Customer name cannot be null");
        Objects.requireNonNull(customerRif, "Customer RIF cannot be null");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must have at least one line");
        }

        LocalDateTime now = LocalDateTime.now();
        return new Order(OrderId.generate(), orderNumber, customerId,
                customerName, customerRif, createdBy, createdByName, status,
                notes, null, null, null, null, lines, now, now);
    }

    public static Order reconstitute(OrderId id, OrderNumber orderNumber,
            UUID customerId, String customerName, String customerRif,
            UUID createdBy, String createdByName, OrderStatus status,
            String notes, UUID invoiceId, String invoiceNumber,
            LocalDateTime cancelledAt, String cancelledReason,
            List<OrderLine> lines, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new Order(id, orderNumber, customerId, customerName, customerRif,
                createdBy, createdByName, status, notes, invoiceId,
                invoiceNumber, cancelledAt, cancelledReason, lines, createdAt,
                updatedAt);
    }

    public void update(String notes, List<OrderLine> lines) {
        if (!isInDraftState()) {
            throw new IllegalStateException("Only DRAFT orders can be updated");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must have at least one line");
        }
        this.notes = notes;
        this.lines.clear();
        this.lines.addAll(lines);
        this.subtotal = calculateSubtotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (!isInDraftState()) {
            throw new IllegalStateException(
                    "Only DRAFT orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (!isInDraftState() && this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only DRAFT or CONFIRMED orders can be cancelled, current: "
                            + this.status);
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot be blank");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInvoiced(UUID invoiceId, String invoiceNumber) {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED orders can be invoiced");
        }
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(invoiceNumber, "Invoice number cannot be null");
        this.status = OrderStatus.INVOICED;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void revertFromInvoiced() {
        if (this.status != OrderStatus.INVOICED) {
            throw new IllegalStateException(
                    "Only INVOICED orders can be reverted from invoiced");
        }
        this.status = OrderStatus.CONFIRMED;
        this.invoiceId = null;
        this.invoiceNumber = null;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isInDraftState() {
        return this.status == OrderStatus.DRAFT;
    }

    private BigDecimal calculateSubtotal() {
        return lines.stream().map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void delete() {
        if (!isInDraftState()) {
            throw new com.cashly.cashly_api.sales.orders.domain.exceptions.OrderCannotBeDeletedException(
                    this.id.value(), this.status);
        }
    }

    public boolean isOwnedBy(UUID userId) {
        return Objects.equals(this.createdBy, userId);
    }

    public OrderId getId() {
        return id;
    }
    public OrderNumber getOrderNumber() {
        return orderNumber;
    }
    public UUID getCustomerId() {
        return customerId;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getCustomerRif() {
        return customerRif;
    }
    public UUID getCreatedBy() {
        return createdBy;
    }
    public String getCreatedByName() {
        return createdByName;
    }
    public OrderStatus getStatus() {
        return status;
    }
    public String getNotes() {
        return notes;
    }
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    public UUID getInvoiceId() {
        return invoiceId;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    public String getCancelledReason() {
        return cancelledReason;
    }
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
