package com.parsun.parsun_api.cashregister.application.services;

import com.parsun.parsun_api.cashregister.domain.entities.CashMovement;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;

import java.math.BigDecimal;
import java.util.UUID;

public class CashMovementService {

    private final CashRegisterRepository registerRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public CashMovementService(CashRegisterRepository registerRepository,
                               PaymentMethodRepository paymentMethodRepository) {
        this.registerRepository = registerRepository;
        this.paymentMethodRepository = paymentMethodRepository;
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
        String paymentMethodName = paymentMethodId == null ? "Sin método" : paymentMethodRepository
                .findById(PaymentMethodId.of(paymentMethodId))
                .map(method -> method.getName())
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

    private com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId activeRegisterId() {
        return registerRepository.findActiveClosure()
                .map(register -> register.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja antes de registrar una operación monetaria"));
    }
}
