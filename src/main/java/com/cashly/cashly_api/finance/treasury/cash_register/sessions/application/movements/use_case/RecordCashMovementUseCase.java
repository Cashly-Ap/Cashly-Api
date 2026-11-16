package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class RecordCashMovementUseCase {

    private final CashRegisterRepository registerRepository;
    private final CashRegisterActorGateway actors;
    private final CashMovementService movementService;
    private final CashRegisterQueryGateway queries;

    private final ActivityNotificationRecorder activityNotifications;

    public RecordCashMovementUseCase(CashRegisterRepository registerRepository,
            CashRegisterActorGateway actors,
            CashMovementService movementService,
            CashRegisterQueryGateway queries,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.registerRepository = registerRepository;
        this.actors = actors;
        this.movementService = movementService;
        this.queries = queries;
    }

    @Transactional
    @Audited(module = "finance", entityType = "CASH_MOVEMENT", action = "RECORD_MOVEMENT")
    public CashMovementResponse execute(UUID registerId,
            CashMovementRequest request, UUID userId) {
        var target = registerRepository.findById(RegisterId.of(registerId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cash register not found: " + registerId));
        if (request.type() != CashMovementType.EXPENSE) {
            requireActiveRegister(target);
        }
        String userName = actors.findById(userId)
                .map(CashRegisterActorGateway.Actor::name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));

        CashMovement movement = request.type() == CashMovementType.EXPENSE
                ? movementService.recordForRegister(target.getId(),
                        request.direction(), request.type(), request.amount(),
                        request.paymentMethodId(), request.description(),
                        "MANUAL", null, userId, userName,
                        request.occurredAt())
                : movementService.record(request.direction(), request.type(),
                        request.amount(), request.paymentMethodId(),
                        request.description(), "MANUAL", null, userId,
                        userName);

        refreshTotals(target, movement);

        if (request.type() == CashMovementType.EXPENSE) {
            activityNotifications.record(ActivityEvent.of(
                    "FINANCE_TREASURY_EXPENSES", "CREATE", "EXPENSE",
                    movement.getId(), movement.getDescription(), userId));
        } else {
            activityNotifications.record(ActivityEvent.of(
                    "FINANCE_TREASURY_CASH_REGISTER", "OPEN", "CASH_REGISTER",
                    target.getId().value(),
                    target.getClosureDate().toString(), userId));
        }
        return CashMovementResponse.from(movement);
    }

    private void requireActiveRegister(CashRegister target) {
        var active = registerRepository.findActiveClosure()
                .orElseThrow(() -> new IllegalStateException(
                        "Debe abrir una caja antes de registrar una operación monetaria"));
        if (!active.getId().equals(target.getId())) {
            throw new IllegalStateException(
                    "Movements can only be added to the active register");
        }
    }

    private void refreshTotals(CashRegister register, CashMovement movement) {
        if (register.getStatus() == RegisterStatus.CLOSED) {
            BigDecimal income = register.getTotalIncome();
            BigDecimal expense = register.getTotalExpense();
            if (movement.getType().affectsCashBalance()) {
                if (movement.getDirection() == CashMovementDirection.IN) {
                    income = income.add(movement.getAmount());
                } else {
                    expense = expense.add(movement.getAmount());
                }
            }
            register.applyMovementTotals(income, expense);
        } else {
            var totals = queries.findSessionTotals(register.getId());
            register.applyMovementTotals(totals.totalRevenue(),
                    totals.totalExpenses());
        }
        registerRepository.save(register);
    }
}
