package com.cashly.cashly_api.finance.billing.credit_notes.application.create.use_case;

import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteEffectsGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteFinancialPositionGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import com.cashly.cashly_api.finance.billing.credit_notes.application.create.request.CreateCreditNoteRequest;
import com.cashly.cashly_api.finance.billing.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.finance.billing.credit_notes.application.mappers.CreditNoteMapper;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.valueobjects.CreditNoteNumber;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateCreditNoteUseCase {
    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteInvoiceGateway invoices;
    private final CreditNoteFinancialPositionGateway financialPosition;
    private final CreditNoteEffectsGateway effects;

    public CreateCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
            CreditNoteInvoiceGateway invoices,
            CreditNoteFinancialPositionGateway financialPosition,
            CreditNoteEffectsGateway effects) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoices = invoices;
        this.financialPosition = financialPosition;
        this.effects = effects;
    }

    @Transactional
    public CreditNoteResponse execute(CreateCreditNoteRequest request,
            UUID authenticatedUserId) {
        CreditNoteInvoiceGateway.CreditNoteInvoiceContext invoice = invoices
                .find(request.invoiceId());
        if ("CANCELLED".equals(invoice.status())
                || "FULLY_RETURNED".equals(invoice.status())) {
            throw new IllegalStateException(
                    "Cannot create credit note for invoice " + invoice.number()
                            + ". Current status: " + invoice.statusLabel());
        }

        int year = LocalDate.now().getYear();
        CreditNoteNumber noteNumber = CreditNoteNumber.of(year,
                creditNoteRepository.getNextSequenceForYear(year));
        CreditNote note = CreditNote.create(noteNumber, request.customerId(),
                request.invoiceId(), request.amount(), request.reason(),
                authenticatedUserId, "User");
        CreditNote saved = creditNoteRepository.save(note);

        effects.recordCashOut(saved.getAmount(), saved.getNoteNumber().value(),
                saved.getId().value(), authenticatedUserId,
                saved.getCreatedByName());
        invoices.recalculate(saved.getInvoiceId());

        CreditNoteFinancialPositionGateway.Position position = financialPosition
                .findByInvoiceId(request.invoiceId());
        BigDecimal effectiveTotal = invoice.totalBeforeNotes()
                .subtract(position.creditNotes()).add(position.debitNotes())
                .max(BigDecimal.ZERO);
        BigDecimal surplus = position.paid().subtract(effectiveTotal);
        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            effects.addCustomerCredit(request.customerId(), surplus,
                    saved.getId().value(), saved.getNoteNumber().value(),
                    invoice.number(), authenticatedUserId);
        }

        return CreditNoteMapper.toCreditNoteResponse(saved, null, null, null);
    }
}
