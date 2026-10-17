package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInventoryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnProjectionQueryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.delete.use_case.DeleteReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.config.ReturnConfig;
import com.cashly.cashly_api.finance.billing.invoices.application.queries.services.RepositoryReturnInvoiceQuery;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.config.ReturnInvoiceQueryConfig;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.config.CreditNoteLedgerQueryConfig;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.config.DebitNoteLedgerQueryConfig;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.config.ReturnPaymentOperationsConfig;
import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.config.ReturnBalanceMutationConfig;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.config.ReturnCashRecordingConfig;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.management.infrastructure.config.WarehouseDirectoryConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class ReturnFinancialWiringTest {
    @Test
    void should_wire_return_owned_gateways_to_neutral_public_providers() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ReturnRepository.class, () -> mock(ReturnRepository.class));
            context.registerBean(ReturnInvoiceAdjustmentGateway.class, () -> mock(ReturnInvoiceAdjustmentGateway.class));
            context.registerBean(ReturnInventoryGateway.class, () -> mock(ReturnInventoryGateway.class));
            context.registerBean(InvoiceRepository.class, () -> mock(InvoiceRepository.class));
            context.registerBean(CreditNoteRepository.class, () -> mock(CreditNoteRepository.class));
            context.registerBean(DebitNoteRepository.class, () -> mock(DebitNoteRepository.class));
            context.registerBean(PaymentRepository.class, () -> mock(PaymentRepository.class));
            context.registerBean(PaymentAllocationRepository.class, () -> mock(PaymentAllocationRepository.class));
            context.registerBean(PaymentAllocationService.class, () -> mock(PaymentAllocationService.class));
            context.registerBean(AddToCustomerBalanceUseCase.class, () -> mock(AddToCustomerBalanceUseCase.class));
            context.registerBean(ConsumeCustomerBalanceUseCase.class, () -> mock(ConsumeCustomerBalanceUseCase.class));
            context.registerBean(CashMovementService.class, () -> mock(CashMovementService.class));
            context.registerBean(WarehouseRepository.class, () -> mock(WarehouseRepository.class));
            context.register(ReturnConfig.class, ReturnMapper.class, com.cashly.cashly_api.finance.billing.returns.application.services.ReturnResponseAssembler.class, ReturnInvoiceQueryConfig.class,
                    CreditNoteLedgerQueryConfig.class, DebitNoteLedgerQueryConfig.class, ReturnPaymentOperationsConfig.class,
                    ReturnBalanceMutationConfig.class, ReturnCashRecordingConfig.class, WarehouseDirectoryConfig.class,
                    LocalReturnInvoiceQueryGateway.class, LocalReturnPaymentSettlementGateway.class,
                    LocalReturnBalanceGateway.class, LocalReturnCashGateway.class, LocalReturnProjectionQueryGateway.class);
            context.refresh();
            assertThat(context.getBean(CreateReturnUseCase.class)).isNotNull();
            assertThat(context.getBean(DeleteReturnUseCase.class)).isNotNull();
            assertThat(context.getBean(ReturnMapper.class)).isNotNull();
            UUID id = UUID.randomUUID();
            assertThat(context.getBean(ReturnProjectionQueryGateway.class).findInvoice(id)).isEmpty();
            assertThat(context.getBean(ReturnProjectionQueryGateway.class).findWarehouseName(id)).isEmpty();
            assertThatThrownBy(() -> context.getBean(ReturnInvoiceQueryGateway.class).requireById(id))
                    .isInstanceOf(com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException.class);
        }
    }
}
