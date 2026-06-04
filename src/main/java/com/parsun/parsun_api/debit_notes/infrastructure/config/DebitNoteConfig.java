package com.parsun.parsun_api.debit_notes.infrastructure.config;

import com.parsun.parsun_api.debit_notes.application.usecases.CreateDebitNoteUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.DeleteDebitNoteUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.GetDebitNoteByIdUseCase;
import com.parsun.parsun_api.debit_notes.application.usecases.GetDebitNotesUseCase;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebitNoteConfig {

    private final DebitNoteRepository debitNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoiceStatusService invoiceStatusService;

    public DebitNoteConfig(DebitNoteRepository debitNoteRepository,
                            InvoiceRepository invoiceRepository,
                            InvoiceJpaRepository invoiceJpaRepository,
                            InvoiceStatusService invoiceStatusService) {
        this.debitNoteRepository = debitNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.invoiceStatusService = invoiceStatusService;
    }

    @Bean
    public CreateDebitNoteUseCase createDebitNoteUseCase() {
        return new CreateDebitNoteUseCase(debitNoteRepository, invoiceRepository, invoiceStatusService);
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
        return new DeleteDebitNoteUseCase(debitNoteRepository, invoiceStatusService);
    }
}
