package com.cashly.cashly_api.returns.infrastructure.integration;
import com.cashly.cashly_api.returns.application.contracts.*;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.application.usecases.CreateReturnUseCase;
import com.cashly.cashly_api.returns.application.usecases.DeleteReturnUseCase;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import com.cashly.cashly_api.returns.infrastructure.config.ReturnConfig;
import com.cashly.cashly_api.invoices.application.services.RepositoryReturnInvoiceQuery;
import com.cashly.cashly_api.invoices.infrastructure.config.ReturnInvoiceQueryConfig;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.infrastructure.config.CreditNoteLedgerQueryConfig;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.infrastructure.config.DebitNoteLedgerQueryConfig;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.payments.infrastructure.config.ReturnPaymentOperationsConfig;
import com.cashly.cashly_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.infrastructure.config.ReturnBalanceMutationConfig;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.infrastructure.config.ReturnCashRecordingConfig;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.infrastructure.config.WarehouseDirectoryConfig;
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
            context.register(ReturnConfig.class, ReturnMapper.class, com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler.class, ReturnInvoiceQueryConfig.class,
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
                    .isInstanceOf(com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException.class);
        }
    }
}
