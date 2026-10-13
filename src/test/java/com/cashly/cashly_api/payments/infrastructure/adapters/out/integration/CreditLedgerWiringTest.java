package com.cashly.cashly_api.payments.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.payments.application.ports.out.*;
import com.cashly.cashly_api.customers.infrastructure.config.CustomerCreditQueryConfig;
import com.cashly.cashly_api.invoices.infrastructure.config.InvoiceCreditQueryConfig;
import com.cashly.cashly_api.credit_notes.infrastructure.config.CreditNoteLedgerQueryConfig;
import com.cashly.cashly_api.debit_notes.infrastructure.config.DebitNoteLedgerQueryConfig;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class CreditLedgerWiringTest {
    @Test
    void should_wire_consumer_gateways_to_public_provider_queries() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(com.cashly.cashly_api.payments.domain.repositories.PaymentRepository.class, () -> mock(com.cashly.cashly_api.payments.domain.repositories.PaymentRepository.class));
            context.registerBean(com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository.class, () -> mock(com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository.class));
            context.registerBean(CustomerRepository.class, () -> mock(CustomerRepository.class));
            context.registerBean(CustomerBalanceRepository.class, () -> mock(CustomerBalanceRepository.class));
            context.registerBean(InvoiceRepository.class, () -> mock(InvoiceRepository.class));
            context.registerBean(CreditNoteRepository.class, () -> mock(CreditNoteRepository.class));
            context.registerBean(DebitNoteRepository.class, () -> mock(DebitNoteRepository.class));
            context.register(CustomerCreditQueryConfig.class, InvoiceCreditQueryConfig.class,
                    CreditNoteLedgerQueryConfig.class, DebitNoteLedgerQueryConfig.class,
                    LocalCustomerCreditContextGateway.class, LocalInvoiceCreditLedgerGateway.class,
                    LocalCreditNoteLedgerGateway.class, LocalDebitNoteLedgerGateway.class,
                    com.cashly.cashly_api.payments.application.services.CustomerCreditQueryService.class,
                    com.cashly.cashly_api.customers.infrastructure.adapters.out.integration.LocalCreditValidationGateway.class,
                    com.cashly.cashly_api.customers.application.services.CustomerCreditEligibilityService.class);
            context.refresh();
            UUID id = UUID.randomUUID();
            assertThat(context.getBean(CustomerCreditContextGateway.class).findById(id)).isEmpty();
            assertThat(context.getBean(InvoiceCreditLedgerGateway.class).findByCustomerId(id)).isEmpty();
            assertThat(context.getBean(InvoiceCreditLedgerGateway.class).findAll()).isEmpty();
            assertThat(context.getBean(CreditNoteLedgerGateway.class).amountsByInvoiceId(id)).isEmpty();
            assertThat(context.getBean(DebitNoteLedgerGateway.class).amountsByInvoiceId(id)).isEmpty();
            assertThatThrownBy(() -> context.getBean(com.cashly.cashly_api.customers.application.ports.out.CreditValidationGateway.class).getCreditStatus(id))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Customer not found: " + id);
        }
    }
}
