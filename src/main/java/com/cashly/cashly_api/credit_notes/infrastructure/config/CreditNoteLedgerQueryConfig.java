package com.cashly.cashly_api.credit_notes.infrastructure.config;
import com.cashly.cashly_api.credit_notes.application.api.CreditNoteLedgerQuery;
import com.cashly.cashly_api.credit_notes.application.services.RepositoryCreditNoteLedgerQuery;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CreditNoteLedgerQueryConfig {
    @Bean
    public CreditNoteLedgerQuery creditNoteLedgerQuery(CreditNoteRepository notes) {
        return new RepositoryCreditNoteLedgerQuery(notes);
    }
}
