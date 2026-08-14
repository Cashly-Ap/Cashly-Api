package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.cashregister.application.dto.CashMovementRequest;
import com.parsun.parsun_api.cashregister.application.dto.CashMovementResponse;
import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class RecordCashMovementUseCase {

    private final CashRegisterRepository registerRepository;
    private final UserRepository userRepository;
    private final CashMovementService movementService;

    public RecordCashMovementUseCase(CashRegisterRepository registerRepository,
                                     UserRepository userRepository,
                                     CashMovementService movementService) {
        this.registerRepository = registerRepository;
        this.userRepository = userRepository;
        this.movementService = movementService;
    }

    @Transactional
    public CashMovementResponse execute(UUID registerId, CashMovementRequest request, UUID userId) {
        var active = registerRepository.findActiveClosure()
                .orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja antes de registrar una operación monetaria"));
        if (!active.getId().equals(RegisterId.of(registerId))) {
            throw new IllegalStateException("Movements can only be added to the active register");
        }
        String userName = userRepository.findById(new UserId(userId))
                .map(user -> user.getUserName().getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        var movement = movementService.record(request.direction(), request.type(), request.amount(),
                request.paymentMethodId(), request.description(), "MANUAL", null, userId, userName);
        return new CashMovementResponse(movement.getId(), movement.getDirection(), movement.getType(),
                movement.getAmount(), movement.getPaymentMethodId(), movement.getPaymentMethodName(),
                movement.getDescription(), movement.getSourceType(), movement.getSourceId(),
                movement.getCreatedBy(), movement.getCreatedByName(), movement.getOccurredAt());
    }
}
