package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.credit_notes.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.application.mappers.CreditNoteMapper;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;
    private final CashMovementService cashMovementService;

    public CreateCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                   InvoiceRepository invoiceRepository,
                                   InvoiceStatusService invoiceStatusService,
                                   PaymentAllocationRepository paymentAllocationRepository,
                                   DebitNoteRepository debitNoteRepository,
                                   AddToCustomerBalanceUseCase addToCustomerBalanceUseCase,
                                   CashMovementService cashMovementService) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.addToCustomerBalanceUseCase = addToCustomerBalanceUseCase;
        this.cashMovementService = cashMovementService;
    }

    @Transactional
    public CreditNoteResponse execute(CreateCreditNoteRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(request.invoiceId()))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.FULLY_RETURNED) {
            throw new IllegalStateException(
                    "Cannot create credit note for invoice " + invoice.getInvoiceNumber().value()
                    + ". Current status: " + invoice.getStatus().label()
            );
        }

        int year = LocalDate.now().getYear();
        int sequence = creditNoteRepository.getNextSequenceForYear(year);
        CreditNoteNumber noteNumber = CreditNoteNumber.of(year, sequence);

        CreditNote creditNote = CreditNote.create(
                noteNumber,
                request.customerId(),
                request.invoiceId(),
                request.amount(),
                request.reason(),
                authenticatedUserId,
                "User"
        );

        CreditNote saved = creditNoteRepository.save(creditNote);
        cashMovementService.record(
                CashMovementDirection.OUT,
                CashMovementType.CREDIT_NOTE,
                saved.getAmount(),
                null,
                "Nota de crédito " + saved.getNoteNumber().value(),
                "CREDIT_NOTE",
                saved.getId().value(),
                authenticatedUserId,
                saved.getCreatedByName()
        );

        invoiceStatusService.recalculate(saved.getInvoiceId());

        BigDecimal totalPaid = paymentAllocationRepository.findByInvoiceId(request.invoiceId()).stream()
                .map(a -> a.getAllocatedAmount() != null ? a.getAllocatedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(request.invoiceId()).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(request.invoiceId()).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            addToCustomerBalanceUseCase.execute(
                request.customerId(),
                BalanceType.CREDIT_NOTE,
                surplus,
                BalanceType.CREDIT_NOTE,
                saved.getId().value(),
                saved.getNoteNumber().value(),
                invoice.getInvoiceNumber().value(),
                authenticatedUserId
            );
        }

        return CreditNoteMapper.toCreditNoteResponse(saved, null, null, null);
    }
}
