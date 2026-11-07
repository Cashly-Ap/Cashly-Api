package com.cashly.cashly_api.finance.billing.debit_notes.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteCashGateway;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.debit_notes.application.create.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.finance.billing.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.finance.billing.debit_notes.application.mappers.DebitNoteMapper;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteNumber;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateDebitNoteUseCase {
    private final DebitNoteRepository debitNoteRepository;
    private final DebitNoteInvoiceGateway invoices;
    private final DebitNoteCashGateway cash;

    private final ActivityNotificationRecorder activityNotifications;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
            DebitNoteInvoiceGateway invoices, DebitNoteCashGateway cash,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.debitNoteRepository = debitNoteRepository;
        this.invoices = invoices;
        this.cash = cash;
    }

    @Transactional
    public DebitNoteResponse execute(CreateDebitNoteRequest request,
            UUID authenticatedUserId) {
        DebitNoteInvoiceGateway.InvoiceContext invoice = invoices
                .find(request.invoiceId());
        if ("CANCELLED".equals(invoice.status())
                || "FULLY_RETURNED".equals(invoice.status())) {
            throw new IllegalStateException(
                    "Cannot create debit note for invoice " + invoice.number()
                            + ". Current status: " + invoice.statusLabel());
        }

        int year = LocalDate.now().getYear();
        DebitNoteNumber noteNumber = DebitNoteNumber.of(year,
                debitNoteRepository.getNextSequenceForYear(year));
        DebitNote note = DebitNote.create(noteNumber, request.customerId(),
                request.invoiceId(), request.amount(), request.reason(),
                authenticatedUserId, "User");
        DebitNote saved = debitNoteRepository.save(note);
        cash.recordCashIn(saved.getAmount(), saved.getNoteNumber().value(),
                saved.getId().value(), authenticatedUserId,
                saved.getCreatedByName());
        invoices.recalculate(saved.getInvoiceId());
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_DEBIT_NOTES",
                "CREATE", "DEBIT_NOTE", saved.getId().value(),
                saved.getNoteNumber().value(), authenticatedUserId));
        return DebitNoteMapper.toDebitNoteResponse(saved, null, null, null);
    }
}
