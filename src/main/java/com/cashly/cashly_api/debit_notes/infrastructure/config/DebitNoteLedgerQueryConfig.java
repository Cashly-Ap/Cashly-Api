package com.cashly.cashly_api.debit_notes.infrastructure.config;
import com.cashly.cashly_api.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.application.services.RepositoryDebitNoteLedgerQuery;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class DebitNoteLedgerQueryConfig {
    @Bean
    public DebitNoteLedgerQuery debitNoteLedgerQuery(DebitNoteRepository notes) {
        return new RepositoryDebitNoteLedgerQuery(notes);
    }
}
