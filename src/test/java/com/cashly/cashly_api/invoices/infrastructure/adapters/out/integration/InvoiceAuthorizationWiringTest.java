package com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.auth.user.infrastructure.config.ActorIdentityQueryConfig;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.infrastructure.config.InvoiceBalanceConsumptionConfig;
import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import com.cashly.cashly_api.cash_register.infrastructure.config.CashRegisterOperationsConfig;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.infrastructure.config.CashInvoiceSettlementConfig;
import com.cashly.cashly_api.payments.infrastructure.adapters.out.integration.LocalPaymentCashRegisterGateway;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerCreditGateway;
import com.cashly.cashly_api.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.application.usecases.ConfirmInvoiceUseCase;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class InvoiceAuthorizationWiringTest {
    @Test
    void should_wire_authorization_and_confirmation_through_all_public_providers() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(UserRepository.class, () -> mock(UserRepository.class));
            context.registerBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class));
            context.registerBean(ConsumeCustomerBalanceUseCase.class, () -> mock(ConsumeCustomerBalanceUseCase.class));
            context.registerBean(PaymentRepository.class, () -> mock(PaymentRepository.class));
            context.registerBean(PaymentAllocationRepository.class, () -> mock(PaymentAllocationRepository.class));
            context.registerBean(CashMovementService.class, () -> mock(CashMovementService.class));
            context.registerBean(InvoiceRepository.class, () -> mock(InvoiceRepository.class));
            context.registerBean(CustomerCreditGateway.class, () -> mock(CustomerCreditGateway.class));
            context.registerBean(InventoryGateway.class, () -> mock(InventoryGateway.class));
            context.registerBean(NotificationService.class, () -> mock(NotificationService.class));
            context.register(ActorIdentityQueryConfig.class, InvoiceBalanceConsumptionConfig.class,
                    CashRegisterOperationsConfig.class, CashInvoiceSettlementConfig.class,
                    LocalActorIdentityGateway.class, LocalCustomerBalanceGateway.class,
                    LocalInvoicePaymentSettlementGateway.class, LocalInvoiceCashRegisterGateway.class,
                    LocalInvoiceNotificationGateway.class, LocalPaymentCashRegisterGateway.class,
                    InvoiceAuthorizationService.class, ConfirmInvoiceUseCase.class);
            context.refresh();
            assertThat(context.getBean(InvoiceAuthorizationService.class)).isNotNull();
            assertThat(context.getBean(ConfirmInvoiceUseCase.class)).isNotNull();
            assertThatThrownBy(() -> context.getBean(ActorIdentityGateway.class).requireActor(UUID.randomUUID()))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Usuario no encontrado");
        }
    }
}
