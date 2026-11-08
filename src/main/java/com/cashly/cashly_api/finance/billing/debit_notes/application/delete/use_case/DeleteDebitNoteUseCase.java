package com.cashly.cashly_api.finance.billing.debit_notes.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class DeleteDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
            DebitNoteInvoiceGateway invoices,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
    }

    @Transactional
    @Audited(module = "finance", entityType = "DEBIT_NOTE", action = "DELETE")
    public void execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Debit note not found: " + noteId));

        UUID invoiceId = debitNote.getInvoiceId();
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_DEBIT_NOTES",
                "DELETE", "DEBIT_NOTE",
                noteId,
                noteId.toString(), null));
        debitNoteRepository.delete(id);

        invoices.recalculate(invoiceId);
    }
}
