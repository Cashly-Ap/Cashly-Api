package com.parsun.parsun_api.finances.domain.entities;

import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnStatus;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Return {

    private final ReturnId id;
    private final ReturnNumber returnNumber;
    private final UUID invoiceId;
    private final UUID customerId;
    private final ReturnType returnType;
    private final String reason;
    private final UUID createdBy;
    private final String createdByName;
    private ReturnStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Return(ReturnId id, ReturnNumber returnNumber, UUID invoiceId, UUID customerId,
                   ReturnType returnType, String reason, UUID createdBy, String createdByName,
                   ReturnStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.returnNumber = returnNumber;
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.returnType = returnType;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Return create(ReturnNumber returnNumber, UUID invoiceId, UUID customerId,
                                ReturnType returnType, String reason, UUID createdBy, String createdByName) {
        Objects.requireNonNull(returnNumber, "Return number cannot be null");
        Objects.requireNonNull(invoiceId, "Invoice id cannot be null");
        Objects.requireNonNull(customerId, "Customer id cannot be null");
        Objects.requireNonNull(returnType, "Return type cannot be null");
        Objects.requireNonNull(reason, "Reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be blank");
        }
        Objects.requireNonNull(createdBy, "Created by cannot be null");
        Objects.requireNonNull(createdByName, "Created by name cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new Return(ReturnId.generate(), returnNumber, invoiceId, customerId, returnType, reason,
                createdBy, createdByName, ReturnStatus.ACTIVE, now, now);
    }

    public static Return reconstitute(ReturnId id, ReturnNumber returnNumber, UUID invoiceId,
                                      UUID customerId, ReturnType returnType, String reason,
                                      UUID createdBy, String createdByName,
                                      ReturnStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Return(id, returnNumber, invoiceId, customerId, returnType, reason,
                createdBy, createdByName, status, createdAt, updatedAt);
    }

    public void cancel() {
        if (this.status == ReturnStatus.CANCELLED) {
            throw new IllegalStateException("Return is already cancelled");
        }
        this.status = ReturnStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public ReturnId getId() { return id; }
    public ReturnNumber getReturnNumber() { return returnNumber; }
    public UUID getInvoiceId() { return invoiceId; }
    public UUID getCustomerId() { return customerId; }
    public ReturnType getReturnType() { return returnType; }
    public String getReason() { return reason; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public ReturnStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
