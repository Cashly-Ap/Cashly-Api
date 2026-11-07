package com.cashly.cashly_api.finance.billing.credit_notes.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteInvoiceGateway invoices;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
            CreditNoteInvoiceGateway invoices,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.creditNoteRepository = creditNoteRepository;
        this.invoices = invoices;
    }

    @Transactional
    public void execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credit note not found: " + noteId));

        UUID invoiceId = creditNote.getInvoiceId();
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_CREDIT_NOTES",
                "DELETE", "CREDIT_NOTE",
                noteId,
                noteId.toString(), null));
        creditNoteRepository.delete(id);

        invoices.recalculate(invoiceId);
    }
}
