package com.cashly.cashly_api.cashregister.application.api;
import java.math.BigDecimal;
import java.util.UUID;
public interface ReturnCashRecording {
    void record(Command command);
    record Command(BigDecimal amount, UUID returnId, String returnNumber, UUID actorId, String actorName) {}
}
