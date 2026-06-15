package com.parsun.parsun_api.invoices.application.services;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.shared.services.CreditGuard;
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
@DisplayName("InvoiceAuthorizationService Unit Tests")
class InvoiceAuthorizationServiceTest {

    @Mock
    private CreditGuard creditGuard;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    private InvoiceAuthorizationService service;

    private final UUID customerId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String confirmationPassword = "Secret1!";
    private final String hashedPassword = "$2a$10$hashedvalue";

    private User user;

    @BeforeEach
    void setUp() {
        service = new InvoiceAuthorizationService(
                creditGuard, passwordEncoder, consumeCustomerBalanceUseCase,
                paymentRepository, allocationRepository
        );

        user = mock(User.class);
        lenient().when(user.getUserName()).thenReturn(new UserName("Test User"));
        var password = mock(Password.class);
        lenient().when(password.getHashedValue()).thenReturn(hashedPassword);
        lenient().when(user.getPassword()).thenReturn(password);
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
                    service.authorize(creditInvoice, user, null, false, null, null, authenticatedUserId));
        }

        @Test
        void should_RequirePassword_When_CreditLimitExceeded() {
            Invoice creditInvoice = invoice(PaymentType.CREDIT);
            when(creditGuard.isCreditAvailable(customerId, creditInvoice.calculateTotal()))
                    .thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(creditInvoice, user, null, false, null, null, authenticatedUserId)
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
                    () -> service.authorize(creditInvoice, user, "", false, null, null, authenticatedUserId)
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
                    service.authorize(creditInvoice, user, confirmationPassword, false, null, null, authenticatedUserId));
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
                    () -> service.authorize(creditInvoice, user, confirmationPassword, false, null, null, authenticatedUserId)
            );

            assertEquals("Contrasena incorrecta", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cash invoice with balance authorization")
    class CashAuthorizationWithBalance {

        @Test
        void should_ConsumeBalance_When_UseBalanceIsTrue() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            BigDecimal balanceAmount = new BigDecimal("500.00");

            when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(allocationRepository.save(any(PaymentAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.authorize(cashInvoice, user, null, true, balanceAmount, paymentMethodId, authenticatedUserId);

            verify(consumeCustomerBalanceUseCase).execute(
                    eq(customerId), eq(BalanceType.INVOICES), eq(balanceAmount),
                    eq(BalanceType.INVOICES), eq(invoiceId), any(), eq(null), eq(authenticatedUserId)
            );
        }

        @Test
        void should_Throw_When_RemainingRequiresPaymentMethodButNoneProvided() {
            Invoice cashInvoice = invoice(PaymentType.CASH);
            BigDecimal balanceAmount = new BigDecimal("200.00");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.authorize(cashInvoice, user, null, true, balanceAmount, null, authenticatedUserId)
            );

            assertEquals("Debe seleccionar un metodo de pago para el monto restante", exception.getMessage());
        }
    }
}
