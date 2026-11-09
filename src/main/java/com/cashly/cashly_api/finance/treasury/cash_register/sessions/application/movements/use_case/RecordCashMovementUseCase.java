package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import jakarta.transaction.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class RecordCashMovementUseCase {

    private final CashRegisterRepository registerRepository;
    private final CashRegisterActorGateway actors;
    private final CashMovementService movementService;

    private final ActivityNotificationRecorder activityNotifications;

    public RecordCashMovementUseCase(CashRegisterRepository registerRepository,
            CashRegisterActorGateway actors,
            CashMovementService movementService,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.registerRepository = registerRepository;
        this.actors = actors;
        this.movementService = movementService;
    }

    @Transactional
    @Audited(module = "finance", entityType = "CASH_MOVEMENT", action = "RECORD_MOVEMENT")
    public CashMovementResponse execute(UUID registerId,
            CashMovementRequest request, UUID userId) {
        var active = registerRepository.findActiveClosure()
                .orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja antes de registrar una operación monetaria"));
        if (!active.getId().equals(RegisterId.of(registerId))) {
            throw new IllegalStateException(
                    "Movements can only be added to the active register");
        }
        String userName = actors.findById(userId)
                .map(CashRegisterActorGateway.Actor::name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));
        var movement = movementService.record(request.direction(),
                request.type(), request.amount(), request.paymentMethodId(),
                request.description(), "MANUAL", null, userId, userName);
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_CASH_REGISTER",
                "OPEN", "CASH_REGISTER", active.getId().value(),
                active.getClosureDate().toString(), userId));
        return new CashMovementResponse(movement.getId(),
                movement.getDirection(), movement.getType(),
                movement.getAmount(), movement.getPaymentMethodId(),
                movement.getPaymentMethodName(), movement.getDescription(),
                movement.getSourceType(), movement.getSourceId(),
                movement.getCreatedBy(), movement.getCreatedByName(),
                movement.getOccurredAt());
    }
}
