package com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.config;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.credit_notes.application.queries.services.RepositoryCreditNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CreditNoteLedgerQueryConfig {
    @Bean
    public CreditNoteLedgerQuery creditNoteLedgerQuery(CreditNoteRepository notes) {
        return new RepositoryCreditNoteLedgerQuery(notes);
    }
}
