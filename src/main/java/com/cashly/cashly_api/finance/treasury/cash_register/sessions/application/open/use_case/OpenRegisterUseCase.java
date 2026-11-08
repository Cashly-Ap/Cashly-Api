package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers.CashRegisterMapper;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class OpenRegisterUseCase {

    private final CashRegisterRepository closureRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public OpenRegisterUseCase(CashRegisterRepository closureRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.closureRepository = closureRepository;
    }

    @Audited(module = "finance", entityType = "CASH_REGISTER", action = "OPEN")
    public CashRegisterResponse execute(UUID openedBy, String openedByName,
            BigDecimal openingBalance) {
        LocalDate today = LocalDate.now();

        if (closureRepository.findActiveClosure().isPresent()) {
            throw new IllegalStateException("A cash register is already open");
        }

        CashRegister closure = CashRegister.open(RegisterId.generate(), today,
                openedBy, openedByName, openingBalance);
        CashRegister saved = closureRepository.save(closure);
        activityNotifications.record(ActivityEvent.of("FINANCE_TREASURY_CASH_REGISTER",
                "OPEN", "CASH_REGISTER", saved.getId().value(),
                saved.getClosureDate().toString(), openedBy));
        return CashRegisterMapper.toResponse(saved);
    }
}
