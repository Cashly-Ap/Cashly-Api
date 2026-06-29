package com.parsun.parsun_api.purchases.domain.entities;

import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseNumber;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseStatus;

import java.math.BigDecimal;
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
    private String notes;
    private BigDecimal subtotal;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancelledReason;
    private final List<PurchaseLine> lines;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Purchase(PurchaseId id, PurchaseNumber purchaseNumber, UUID supplierId,
                     String supplierName, String supplierRif,
                     UUID createdBy, String createdByName,
                     PurchaseStatus status, String notes, BigDecimal subtotal,
                     LocalDateTime confirmedAt, LocalDateTime cancelledAt, String cancelledReason,
                     List<PurchaseLine> lines, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.purchaseNumber = purchaseNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierRif = supplierRif;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.status = status;
        this.notes = notes;
        this.subtotal = subtotal;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
        this.lines = new ArrayList<>(lines != null ? lines : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Purchase create(PurchaseNumber purchaseNumber, UUID supplierId,
                                   String supplierName, String supplierRif,
                                   UUID createdBy, String createdByName,
                                   String notes, List<PurchaseLine> lines) {
        Objects.requireNonNull(purchaseNumber, "Purchase number cannot be null");
        Objects.requireNonNull(supplierId, "Supplier id cannot be null");
        Objects.requireNonNull(supplierName, "Supplier name cannot be null");
        Objects.requireNonNull(supplierRif, "Supplier RIF cannot be null");
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Purchase must have at least one line");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal calculatedSubtotal = calculateSubtotal(lines);

        return new Purchase(PurchaseId.generate(), purchaseNumber, supplierId, supplierName,
                supplierRif, createdBy, createdByName, PurchaseStatus.DRAFT, notes,
                calculatedSubtotal, null, null, null, lines, now, now);
    }

    public static Purchase reconstitute(PurchaseId id, PurchaseNumber purchaseNumber,
                                         UUID supplierId, String supplierName, String supplierRif,
                                         UUID createdBy, String createdByName,
                                         PurchaseStatus status, String notes, BigDecimal subtotal,
                                         LocalDateTime confirmedAt, LocalDateTime cancelledAt,
                                         String cancelledReason,
                                         List<PurchaseLine> lines, LocalDateTime createdAt,
                                         LocalDateTime updatedAt) {
        return new Purchase(id, purchaseNumber, supplierId, supplierName, supplierRif,
                createdBy, createdByName, status, notes, subtotal,
                confirmedAt, cancelledAt, cancelledReason, lines, createdAt, updatedAt);
    }

    public void update(String notes, List<PurchaseLine> lines) {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchases can be updated");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Purchase must have at least one line");
        }
        this.notes = notes;
        this.lines.clear();
        this.lines.addAll(lines);
        this.subtotal = calculateSubtotal(this.lines);
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchases can be confirmed");
        }
        this.status = PurchaseStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status != PurchaseStatus.DRAFT && this.status != PurchaseStatus.CONFIRMED) {
            throw new IllegalStateException("Only DRAFT or CONFIRMED purchases can be cancelled, current: " + this.status);
        }
        Objects.requireNonNull(reason, "Cancellation reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }
        this.status = PurchaseStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.status != PurchaseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchases can be deleted");
        }
    }

    private static BigDecimal calculateSubtotal(List<PurchaseLine> lines) {
        return lines.stream()
                .map(PurchaseLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PurchaseId getId() { return id; }
    public PurchaseNumber getPurchaseNumber() { return purchaseNumber; }
    public UUID getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierRif() { return supplierRif; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public PurchaseStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public BigDecimal getSubtotal() { return subtotal; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancelledReason() { return cancelledReason; }
    public List<PurchaseLine> getLines() { return Collections.unmodifiableList(lines); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
