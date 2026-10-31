package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.infrastructure.config.ActorIdentityQueryConfig;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.config.InvoiceBalanceConsumptionConfig;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.config.CashRegisterOperationsConfig;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.config.CashInvoiceSettlementConfig;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration.LocalPaymentCashRegisterGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case.ConfirmInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class InvoiceAuthorizationWiringTest {
    @Test
    void should_wire_authorization_and_confirmation_through_all_public_providers() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(UserRepository.class,
                    () -> mock(UserRepository.class));
            context.registerBean(PasswordEncoder.class,
                    () -> mock(PasswordEncoder.class));
            context.registerBean(ConsumeCustomerBalanceUseCase.class,
                    () -> mock(ConsumeCustomerBalanceUseCase.class));
            context.registerBean(PaymentRepository.class,
                    () -> mock(PaymentRepository.class));
            context.registerBean(PaymentAllocationRepository.class,
                    () -> mock(PaymentAllocationRepository.class));
            context.registerBean(CashMovementService.class,
                    () -> mock(CashMovementService.class));
            context.registerBean(InvoiceRepository.class,
                    () -> mock(InvoiceRepository.class));
            context.registerBean(CustomerCreditGateway.class,
                    () -> mock(CustomerCreditGateway.class));
            context.registerBean(
                    com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway.class,
                    () -> mock(
                            com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway.class));
            context.registerBean(InventoryGateway.class,
                    () -> mock(InventoryGateway.class));
            context.registerBean(NotificationService.class,
                    () -> mock(NotificationService.class));
            context.register(ActorIdentityQueryConfig.class,
                    InvoiceBalanceConsumptionConfig.class,
                    CashRegisterOperationsConfig.class,
                    CashInvoiceSettlementConfig.class,
                    LocalActorIdentityGateway.class,
                    LocalCustomerBalanceGateway.class,
                    LocalInvoicePaymentSettlementGateway.class,
                    LocalInvoiceCashRegisterGateway.class,
                    LocalInvoiceNotificationGateway.class,
                    LocalPaymentCashRegisterGateway.class,
                    InvoiceIssueValidator.class, InvoicePaymentService.class,
                    IssueInvoiceService.class, ConfirmInvoiceUseCase.class);
            context.refresh();
            assertThat(context.getBean(IssueInvoiceService.class)).isNotNull();
            assertThat(context.getBean(ConfirmInvoiceUseCase.class))
                    .isNotNull();
            assertThatThrownBy(() -> context.getBean(ActorIdentityGateway.class)
                    .requireActor(UUID.randomUUID()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Usuario no encontrado");
        }
    }
}
