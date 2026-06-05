package com.parsun.parsun_api.credit_notes.infrastructure.config;

import com.parsun.parsun_api.credit_notes.application.usecases.CreateCreditNoteUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.DeleteCreditNoteUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.GetCreditNoteByIdUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.GetCreditNotesUseCase;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditNoteConfig {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;

    public CreditNoteConfig(CreditNoteRepository creditNoteRepository,
                            InvoiceRepository invoiceRepository,
                            InvoiceJpaRepository invoiceJpaRepository,
                            InvoiceStatusService invoiceStatusService,
                            PaymentAllocationRepository paymentAllocationRepository,
                            DebitNoteRepository debitNoteRepository,
                            AddToCustomerBalanceUseCase addToCustomerBalanceUseCase) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.addToCustomerBalanceUseCase = addToCustomerBalanceUseCase;
    }

    @Bean
    public CreateCreditNoteUseCase createCreditNoteUseCase() {
        return new CreateCreditNoteUseCase(creditNoteRepository, invoiceRepository, invoiceStatusService,
                paymentAllocationRepository, debitNoteRepository, addToCustomerBalanceUseCase);
    }

    @Bean
    public GetCreditNotesUseCase getCreditNotesUseCase() {
        return new GetCreditNotesUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public GetCreditNoteByIdUseCase getCreditNoteByIdUseCase() {
        return new GetCreditNoteByIdUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public DeleteCreditNoteUseCase deleteCreditNoteUseCase() {
        return new DeleteCreditNoteUseCase(creditNoteRepository, invoiceStatusService);
    }
}
