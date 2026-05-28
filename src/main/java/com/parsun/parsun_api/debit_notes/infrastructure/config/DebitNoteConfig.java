package com.parsun.parsun_api.debit_notes.infrastructure.config;

import com.parsun.parsun_api.debit_notes.application.usecases.CreateDebitNoteUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.DeleteDebitNoteUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.GetDebitNoteByIdUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.GetDebitNotesUseCase;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebitNoteConfig {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    public DebitNoteConfig(DebitNoteRepository debitNoteRepository,
                            InvoiceJpaRepository invoiceJpaRepository) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Bean
    public CreateDebitNoteUseCase createDebitNoteUseCase() {
        return new CreateDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public GetDebitNotesUseCase getDebitNotesUseCase() {
        return new GetDebitNotesUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public GetDebitNoteByIdUseCase getDebitNoteByIdUseCase() {
        return new GetDebitNoteByIdUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public DeleteDebitNoteUseCase deleteDebitNoteUseCase() {
        return new DeleteDebitNoteUseCase(debitNoteRepository);
    }
}
