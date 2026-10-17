package com.cashly.cashly_api.finance.billing.invoices.application.issue.services;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoiceIssuanceFinancialFlowTest;

import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Invoice issuance validation and payment flow")
class InvoiceIssuanceFinancialFlowTest {

    @Mock
    private CustomerCreditGateway creditGuard;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    @Mock
    private CashMovementService cashMovementService;

    private AuthorizationFlow service;

    private final UUID customerId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String confirmationPassword = "Secret1!";
    private final String hashedPassword = "$2a$10$hashedvalue";

    private User user;
    private com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway.Actor actor;
    private com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository actorRepository;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getUserName()).thenReturn(new UserName("Test User"));
        var password = mock(Password.class);
        lenient().when(password.getHashedValue()).thenReturn(hashedPassword);
        lenient().when(user.getPassword()).thenReturn(password);
        actorRepository = mock(com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository.class);
        lenient().when(actorRepository.findById(new UserId(authenticatedUserId))).thenReturn(java.util.Optional.of(user));
        actor = new com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway.Actor(authenticatedUserId, "Test User");
        var cashOperations = new com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.services.CashRegisterOperationsService(cashMovementService);
        service = new AuthorizationFlow(creditGuard,
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalActorIdentityGateway(
                        new com.cashly.cashly_api.platform.auth.user.application.queries.services.RepositoryActorIdentityQuery(actorRepository, passwordEncoder)),
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalCustomerBalanceGateway(
                        new com.cashly.cashly_api.partners.customers.balance.application.services.InvoiceBalanceConsumptionService(consumeCustomerBalanceUseCase)),
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoicePaymentSettlementGateway(
                        new com.cashly.cashly_api.finance.treasury.payments.collection.application.services.CashInvoiceSettlementService(paymentRepository, allocationRepository,
                                new com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration.LocalPaymentCashRegisterGateway(cashOperations))),
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceCashRegisterGateway(cashOperations));
    }

    private Invoice invoice(PaymentType paymentType) {
        return Invoice.reconstitute(
                InvoiceId.of(invoiceId),
                InvoiceNumber.of(2026, 1),
                customerId,
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator",
                null, null,
                InvoiceStatus.PENDING_PAYMENT,
                paymentType,
                null,
                BigDecimal.ZERO,
                null,
                null, null,
                List.of(InvoiceLine.create(
                        UUID.randomUUID(), "Product", "P001",
                        new BigDecimal("5"), new BigDecimal("200.00"),
                        List.of()
                )),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atStartOfDay()
        );
    }

    @Nested
    @DisplayName("Credit invoice authorization")
    class CreditAuthorization {

        @Test
        void should_AuthorizeCredit_When_CreditIsAvailable() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(true);

            assertDoesNotThrow(() ->
                    service.authorize(creditInvoice, actor, null, false, null, null, authenticatedUserId));
        }

        @Test
        void should_RequirePassword_When_CreditLimitExceeded() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(creditInvoice, actor, null, false, null, null, authenticatedUserId)
            );

            assertEquals(
                    "Se requiere contrasena para confirmar facturas que exceden el limite de credito",
                    exception.getMessage());
        }

        @Test
        void should_RequirePassword_When_ConfirmationPasswordIsBlank() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(creditInvoice, actor, "", false, null, null, authenticatedUserId)
            );

            assertEquals(
                    "Se requiere contrasena para confirmar facturas que exceden el limite de credito",
                    exception.getMessage());
        }

        @Test
        void should_Authorize_When_PasswordMatches() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(false);
            when(passwordEncoder.matches(confirmationPassword, hashedPassword))
                    .thenReturn(true);

            assertDoesNotThrow(() ->
                    service.authorize(creditInvoice, actor, confirmationPassword, false, null, null, authenticatedUserId));
        }

        @Test
        void should_Throw_When_PasswordDoesNotMatch() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(false);
            when(passwordEncoder.matches(confirmationPassword, hashedPassword))
                    .thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(creditInvoice, actor, confirmationPassword, false, null, null, authenticatedUserId)
            );

            assertEquals("Contrasena incorrecta", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cash invoice with balance authorization")
    class CashAuthorizationWithBalance {

        @Test
        void should_CreateFullPayment_When_CashInvoiceDoesNotUseBalance() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(allocationRepository.save(any(PaymentAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.authorize(cashInvoice, actor, null, false, null, paymentMethodId, authenticatedUserId);

            verify(consumeCustomerBalanceUseCase, never()).execute(
                    any(), any(), any(), any(), any(), any(), any(), any());
            verify(cashMovementService).record(
                    eq(CashMovementDirection.IN), eq(CashMovementType.PAYMENT),
                    eq(new BigDecimal("1000.00")), eq(paymentMethodId),
                    eq("Pago de factura " + cashInvoice.getInvoiceNumber().value()),
                    eq("PAYMENT"), org.mockito.ArgumentMatchers.any(UUID.class),
                    eq(authenticatedUserId), eq("Test User"));
        }

        @Test
        void should_RequirePaymentMethod_When_CashInvoiceDoesNotUseBalance() {
            Invoice cashInvoice = invoice(PaymentType.CASH);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(cashInvoice, actor, null, false, null, null, authenticatedUserId)
            );

            assertEquals("Debe seleccionar un metodo de pago para el monto restante", exception.getMessage());
        }

        @Test
        void should_ConsumeBalance_When_UseBalanceIsTrue() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            BigDecimal balanceAmount = new BigDecimal("500.00");

            when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(allocationRepository.save(any(PaymentAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.authorize(cashInvoice, actor, null, true, balanceAmount, paymentMethodId, authenticatedUserId);

            verify(consumeCustomerBalanceUseCase).execute(
                    eq(customerId), eq(BalanceType.INVOICES), eq(balanceAmount),
                    eq(BalanceType.INVOICES), eq(invoiceId), any(), eq(null), eq(authenticatedUserId)
            );
            verify(cashMovementService).record(
                    eq(CashMovementDirection.IN), eq(CashMovementType.PAYMENT),
                    eq(new BigDecimal("500.00")), eq(paymentMethodId),
                    eq("Pago de factura " + cashInvoice.getInvoiceNumber().value()),
                    eq("PAYMENT"), org.mockito.ArgumentMatchers.any(UUID.class),
                    eq(authenticatedUserId), eq("Test User"));
        }

        @Test
        void should_Throw_When_RemainingRequiresPaymentMethodButNoneProvided() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            BigDecimal balanceAmount = new BigDecimal("200.00");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(cashInvoice, actor, null, true, balanceAmount, null, authenticatedUserId)
            );

            assertEquals("Debe seleccionar un metodo de pago para el monto restante", exception.getMessage());
        }
        @Test
        void should_fail_closed_register_before_consuming_customer_balance() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            org.mockito.Mockito.doThrow(new IllegalStateException("Register closed"))
                    .when(cashMovementService).requireOpenRegister();
            var error = assertThrows(IllegalStateException.class, () -> service.authorize(cashInvoice,
                    actor, null, true, new BigDecimal("500.00"), paymentMethodId, authenticatedUserId));
            assertEquals("Register closed", error.getMessage());
            verify(consumeCustomerBalanceUseCase, never()).execute(any(), any(), any(), any(), any(), any(), any(), any());
            org.mockito.Mockito.verifyNoInteractions(paymentRepository, allocationRepository);
        }

        @Test
        void should_not_consume_balance_when_payment_method_is_missing() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            assertThrows(IllegalArgumentException.class, () -> service.authorize(cashInvoice,
                    actor, null, true, new BigDecimal("500.00"), null, authenticatedUserId));
            verify(consumeCustomerBalanceUseCase, never()).execute(any(), any(), any(), any(), any(), any(), any(), any());
            org.mockito.Mockito.verifyNoInteractions(cashMovementService, paymentRepository, allocationRepository);
        }

        @Test
        void should_consume_full_total_without_payment_when_balance_amount_is_absent() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            service.authorize(cashInvoice, actor, null, true, null, null, authenticatedUserId);
            verify(consumeCustomerBalanceUseCase).execute(eq(customerId), eq(BalanceType.INVOICES),
                    eq(new BigDecimal("1000.00")), eq(BalanceType.INVOICES), eq(invoiceId),
                    eq(cashInvoice.getInvoiceNumber().value()), eq(null), eq(authenticatedUserId));
            org.mockito.Mockito.verifyNoInteractions(cashMovementService, paymentRepository, allocationRepository);
        }
        @Test
        void should_require_open_register_before_balance_debit_and_payment_recording() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(allocationRepository.save(any(PaymentAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
            service.authorize(cashInvoice, actor, null, true, new BigDecimal("500.00"), paymentMethodId, authenticatedUserId);
            var ordered = org.mockito.Mockito.inOrder(cashMovementService, consumeCustomerBalanceUseCase);
            ordered.verify(cashMovementService).requireOpenRegister();
            ordered.verify(consumeCustomerBalanceUseCase).execute(eq(customerId), eq(BalanceType.INVOICES), eq(new BigDecimal("500.00")),
                    eq(BalanceType.INVOICES), eq(invoiceId), eq(cashInvoice.getInvoiceNumber().value()), eq(null), eq(authenticatedUserId));
            ordered.verify(cashMovementService).record(eq(CashMovementDirection.IN), eq(CashMovementType.PAYMENT), eq(new BigDecimal("500.00")),
                    eq(paymentMethodId), eq("Pago de factura " + cashInvoice.getInvoiceNumber().value()), eq("PAYMENT"),
                    any(UUID.class), eq(authenticatedUserId), eq("Test User"));
            ordered.verifyNoMoreInteractions();
        }
    }
    private static class AuthorizationFlow {
        private final InvoiceIssueValidator validator;
        private final InvoicePaymentService payments;
        AuthorizationFlow(CustomerCreditGateway credit,
                com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway actors,
                com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerBalanceGateway balances,
                com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoicePaymentSettlementGateway settlements,
                com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CashRegisterGateway cashRegister) {
            validator = new InvoiceIssueValidator(credit, actors, cashRegister);
            payments = new InvoicePaymentService(balances, settlements);
        }
        void authorize(Invoice invoice,
                com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway.Actor actor,
                String password, Boolean useBalance, BigDecimal balanceAmount, UUID method, UUID userId) {
            var plan = validator.validate(invoice, actor, password, useBalance, balanceAmount, method, userId);
            payments.apply(invoice, plan, new IssueInvoiceCommand(password, useBalance, balanceAmount, method), actor);
        }
    }

}
