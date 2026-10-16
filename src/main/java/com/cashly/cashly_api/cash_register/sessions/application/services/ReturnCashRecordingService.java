package com.cashly.cashly_api.cash_register.sessions.application.services;

import com.cashly.cashly_api.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.cash_register.sessions.application.ports.in.ReturnCashRecording;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import org.springframework.transaction.annotation.Transactional;
public class ReturnCashRecordingService implements ReturnCashRecording {
    private final CashMovementService movements;
    public ReturnCashRecordingService(CashMovementService movements) { this.movements = movements; }
    @Transactional
    public void record(Command command) {
        movements.record(CashMovementDirection.OUT, CashMovementType.RETURN, command.amount(), null,
                "Devolución " + command.returnNumber(), "RETURN", command.returnId(), command.actorId(), command.actorName());
    }
}
