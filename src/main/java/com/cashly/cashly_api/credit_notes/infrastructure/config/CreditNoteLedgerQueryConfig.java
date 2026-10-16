package com.cashly.cashly_api.credit_notes.infrastructure.config;
import com.cashly.cashly_api.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.credit_notes.application.queries.RepositoryCreditNoteLedgerQuery;
import com.cashly.cashly_api.credit_notes.domain.CreditNoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CreditNoteLedgerQueryConfig {
    @Bean
    public CreditNoteLedgerQuery creditNoteLedgerQuery(CreditNoteRepository notes) {
        return new RepositoryCreditNoteLedgerQuery(notes);
    }
}
