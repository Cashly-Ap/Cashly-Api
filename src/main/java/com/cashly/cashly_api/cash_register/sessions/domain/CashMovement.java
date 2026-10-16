package com.cashly.cashly_api.cash_register.sessions.domain;

import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CashMovement {

    private final UUID id;
    private final RegisterId registerId;
    private final CashMovementDirection direction;
    private final CashMovementType type;
    private final BigDecimal amount;
    private final UUID paymentMethodId;
    private final String paymentMethodName;
    private final String description;
    private final String sourceType;
    private final UUID sourceId;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime occurredAt;

    private CashMovement(UUID id, RegisterId registerId, CashMovementDirection direction,
                         CashMovementType type, BigDecimal amount, UUID paymentMethodId,
                         String paymentMethodName, String description, String sourceType,
                         UUID sourceId, UUID createdBy, String createdByName,
                         LocalDateTime occurredAt) {
        this.id = Objects.requireNonNull(id);
        this.registerId = Objects.requireNonNull(registerId);
        this.direction = Objects.requireNonNull(direction);
        this.type = Objects.requireNonNull(type);
        validateDirection(direction, type);
        this.amount = requirePositive(amount);
        this.paymentMethodId = paymentMethodId;
        this.paymentMethodName = paymentMethodName;
        this.description = Objects.requireNonNull(description);
        this.sourceType = Objects.requireNonNull(sourceType);
        this.sourceId = sourceId;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public static CashMovement create(RegisterId registerId, CashMovementDirection direction,
                                      CashMovementType type, BigDecimal amount,
                                      UUID paymentMethodId, String paymentMethodName,
                                      String description, String sourceType, UUID sourceId,
                                      UUID createdBy, String createdByName) {
        return new CashMovement(UUID.randomUUID(), registerId, direction, type, amount,
                paymentMethodId, paymentMethodName, description, sourceType, sourceId,
                createdBy, createdByName, LocalDateTime.now());
    }

    public static CashMovement reconstitute(UUID id, RegisterId registerId,
                                             CashMovementDirection direction,
                                             CashMovementType type, BigDecimal amount,
                                             UUID paymentMethodId, String paymentMethodName,
                                             String description, String sourceType, UUID sourceId,
                                             UUID createdBy, String createdByName,
                                             LocalDateTime occurredAt) {
        return new CashMovement(id, registerId, direction, type, amount, paymentMethodId,
                paymentMethodName, description, sourceType, sourceId, createdBy,
                createdByName, occurredAt);
    }

    private static BigDecimal requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return amount;
    }

    private static void validateDirection(CashMovementDirection direction, CashMovementType type) {
        boolean valid = switch (type) {
            case PAYMENT, ADVANCE, INVOICE, DEBIT_NOTE, MANUAL_INCOME -> direction == CashMovementDirection.IN;
            case REFUND, CREDIT_NOTE, RETURN, EXPENSE, WITHDRAWAL -> direction == CashMovementDirection.OUT;
            case ADJUSTMENT -> true;
        };
        if (!valid) {
            throw new IllegalArgumentException("Movement direction does not match its type");
        }
    }

    public UUID getId() { return id; }
    public RegisterId getRegisterId() { return registerId; }
    public CashMovementDirection getDirection() { return direction; }
    public CashMovementType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public String getPaymentMethodName() { return paymentMethodName; }
    public String getDescription() { return description; }
    public String getSourceType() { return sourceType; }
    public UUID getSourceId() { return sourceId; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
