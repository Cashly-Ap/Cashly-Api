package com.cashly.cashly_api.returns.infrastructure.integration;
import com.cashly.cashly_api.returns.application.contracts.ReturnCashGateway;
import com.cashly.cashly_api.cashregister.application.api.ReturnCashRecording;
import org.springframework.stereotype.Component;
@Component
public class LocalReturnCashGateway implements ReturnCashGateway {
    private final ReturnCashRecording recording;
    public LocalReturnCashGateway(ReturnCashRecording recording) { this.recording = recording; }
    public void record(Entry entry) { recording.record(new ReturnCashRecording.Command(entry.amount(), entry.returnId(), entry.returnNumber(), entry.actorId(), entry.actorName())); }
}
