package com.cashly.cashly_api.cashregister.application.services;
import com.cashly.cashly_api.cashregister.application.api.ReturnCashRecording;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
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
