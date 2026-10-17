package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalCreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalCustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalDebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalInvoiceCreditLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.customers.credit.infrastructure.config.CustomerCreditQueryConfig;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.config.InvoiceCreditQueryConfig;
import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.config.CreditNoteLedgerQueryConfig;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.config.DebitNoteLedgerQueryConfig;
import com.cashly.cashly_api.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class CreditLedgerWiringTest {
    @Test
    void should_wire_consumer_gateways_to_public_provider_queries() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository.class, () -> mock(com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository.class));
            context.registerBean(com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository.class, () -> mock(com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository.class));
            context.registerBean(CustomerRepository.class, () -> mock(CustomerRepository.class));
            context.registerBean(CustomerBalanceRepository.class, () -> mock(CustomerBalanceRepository.class));
            context.registerBean(InvoiceRepository.class, () -> mock(InvoiceRepository.class));
            context.registerBean(CreditNoteRepository.class, () -> mock(CreditNoteRepository.class));
            context.registerBean(DebitNoteRepository.class, () -> mock(DebitNoteRepository.class));
            context.register(CustomerCreditQueryConfig.class, InvoiceCreditQueryConfig.class,
                    CreditNoteLedgerQueryConfig.class, DebitNoteLedgerQueryConfig.class,
                    LocalCustomerCreditContextGateway.class, LocalInvoiceCreditLedgerGateway.class,
                    LocalCreditNoteLedgerGateway.class, LocalDebitNoteLedgerGateway.class,
                    com.cashly.cashly_api.finance.treasury.payments.credit.application.services.CustomerCreditQueryService.class,
                    com.cashly.cashly_api.customers.credit.infrastructure.integration.LocalCreditValidationGateway.class,
                    com.cashly.cashly_api.customers.credit.application.services.CustomerCreditEligibilityService.class);
            context.refresh();
            UUID id = UUID.randomUUID();
            assertThat(context.getBean(CustomerCreditContextGateway.class).findById(id)).isEmpty();
            assertThat(context.getBean(InvoiceCreditLedgerGateway.class).findByCustomerId(id)).isEmpty();
            assertThat(context.getBean(InvoiceCreditLedgerGateway.class).findAll()).isEmpty();
            assertThat(context.getBean(CreditNoteLedgerGateway.class).amountsByInvoiceId(id)).isEmpty();
            assertThat(context.getBean(DebitNoteLedgerGateway.class).amountsByInvoiceId(id)).isEmpty();
            assertThatThrownBy(() -> context.getBean(com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway.class).getCreditStatus(id))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Customer not found: " + id);
        }
    }
}
