package com.cashly.cashly_api.cash_register.sessions.application.movements.services;

import com.cashly.cashly_api.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cash_register.sessions.application.ports.out.CashRegisterPaymentMethodGateway;

import java.math.BigDecimal;
import java.util.UUID;

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

    public CashMovement record(CashMovementDirection direction, CashMovementType type,
                               BigDecimal amount, UUID paymentMethodId, String description,
                               String sourceType, UUID sourceId, UUID createdBy,
                               String createdByName) {
        if (sourceId != null && registerRepository.existsMovement(sourceType, sourceId)) {
            throw new IllegalStateException("The monetary operation is already registered in cash");
        }
        var registerId = activeRegisterId();
        String paymentMethodName = paymentMethodId == null ? "Sin método" : paymentMethods
                .findById(paymentMethodId)
                .map(CashRegisterPaymentMethodGateway.PaymentMethod::name)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));
        CashMovement movement = CashMovement.create(registerId, direction, type, amount,
                paymentMethodId, paymentMethodName, normalizeDescription(description, type),
                sourceType, sourceId,
                createdBy, createdByName);
        return registerRepository.saveMovement(movement);
    }

    private String normalizeDescription(String description, CashMovementType type) {
        return description == null || description.isBlank() ? type.name() : description;
    }

    private com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterId activeRegisterId() {
        return registerRepository.findActiveClosure()
                .map(register -> register.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja antes de registrar una operación monetaria"));
    }
}
