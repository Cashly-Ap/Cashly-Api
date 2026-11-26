package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterPaymentMethodGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in.CashRegisterOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CashMovementService {

    private final CashRegisterRepository registerRepository;
    private final CashRegisterPaymentMethodGateway paymentMethods;

    public CashMovementService(CashRegisterRepository registerRepository,
            CashRegisterPaymentMethodGateway paymentMethods) {
        this.registerRepository = registerRepository;
        this.paymentMethods = paymentMethods;
    }

    public void requireOpenRegister() {
        activeRegisterId();
    }

    public boolean hasOpenRegister() {
        return registerRepository.findActiveClosure().isPresent();
    }

    public void cancelBySource(String sourceType, UUID sourceId, String reason,
            UUID actorId, String actorName) {
        if (sourceType == null || sourceId == null) {
            return;
        }
        registerRepository.findMovementBySource(sourceType, sourceId)
                .filter(movement -> !movement.isCancelled())
                .ifPresent(movement -> {
                    movement.cancel(reason, actorId, actorName);
                    registerRepository.saveMovement(movement);
                });
    }

    public void recordAll(Collection<CashRegisterOperations.Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        RegisterId registerId = activeRegisterId();
        Map<UUID, String> methodNames = new HashMap<>();
        java.util.List<CashMovement> movements = new java.util.ArrayList<>(
                entries.size());
        for (CashRegisterOperations.Entry entry : entries) {
            CashMovementDirection direction = CashMovementDirection
                    .valueOf(entry.direction().name());
            CashMovementType type = CashMovementType
                    .valueOf(entry.type().name());
            String methodName = entry.paymentMethodId() == null
                    ? ""
                    : methodNames.computeIfAbsent(entry.paymentMethodId(),
                            id -> paymentMethods.findById(id)
                                    .map(CashRegisterPaymentMethodGateway.PaymentMethod::name)
                                    .orElseThrow(() -> new IllegalArgumentException(
                                            "Payment method not found: " + id)));
            movements.add(CashMovement.create(registerId, direction, type,
                    entry.amount(), entry.paymentMethodId(), methodName,
                    normalizeDescription(entry.description(), type),
                    entry.sourceType(), entry.sourceId(), entry.actorId(),
                    entry.actorName(), null));
        }
    }

    public CashMovement record(CashMovementDirection direction,
            CashMovementType type, BigDecimal amount, UUID paymentMethodId,
            String description, String sourceType, UUID sourceId,
            UUID createdBy, String createdByName) {
        return recordForRegister(activeRegisterId(), direction, type, amount,
                paymentMethodId, description, sourceType, sourceId, createdBy,
                createdByName, null);
    }

    public CashMovement recordForRegister(RegisterId registerId,
            CashMovementDirection direction, CashMovementType type,
            BigDecimal amount, UUID paymentMethodId, String description,
            String sourceType, UUID sourceId, UUID createdBy,
            String createdByName, LocalDateTime occurredAt) {
        if (sourceId != null
                && registerRepository.existsMovement(sourceType, sourceId)) {
            throw new IllegalStateException(
                    "The monetary operation is already registered in cash");
        }
        String paymentMethodName = paymentMethodId == null
                ? ""
                : paymentMethods.findById(paymentMethodId).map(
                        CashRegisterPaymentMethodGateway.PaymentMethod::name)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Payment method not found: "
                                        + paymentMethodId));
        CashMovement movement = CashMovement.create(registerId, direction, type,
                amount, paymentMethodId, paymentMethodName,
                normalizeDescription(description, type), sourceType, sourceId,
                createdBy, createdByName, occurredAt);
        return registerRepository.saveMovement(movement);
    }

    private String normalizeDescription(String description,
            CashMovementType type) {
        return description == null || description.isBlank()
                ? type.name()
                : description;
    }

    private RegisterId activeRegisterId() {
        return registerRepository.findActiveClosure()
                .map(register -> register.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "A cash register must be open before registering a monetary operation"));
    }
}
