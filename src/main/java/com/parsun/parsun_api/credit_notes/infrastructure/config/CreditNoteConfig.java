package com.parsun.parsun_api.credit_notes.infrastructure.config;

import com.parsun.parsun_api.credit_notes.application.usecases.CreateCreditNoteUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.DeleteCreditNoteUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.GetCreditNoteByIdUseCase;
import com.parsun.parsun_api.credit_notes.application.usecases.GetCreditNotesUseCase;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditNoteConfig {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public CreditNoteConfig(CreditNoteRepository creditNoteRepository,
                            InvoiceJpaRepository invoiceJpaRepository) {
        this.creditNoteRepository = creditNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Bean
    public CreateCreditNoteUseCase createCreditNoteUseCase() {
        return new CreateCreditNoteUseCase(creditNoteRepository);
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
        return new DeleteCreditNoteUseCase(creditNoteRepository);
    }
}
