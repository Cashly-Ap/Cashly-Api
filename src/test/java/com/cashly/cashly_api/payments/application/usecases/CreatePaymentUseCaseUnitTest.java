package com.cashly.cashly_api.payments.application.usecases;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.payments.application.dto.request.CreatePaymentRequest;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePaymentUseCase Unit Tests")
class CreatePaymentUseCaseUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CashMovementService cashMovementService;

    private CreatePaymentUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreatePaymentUseCase(paymentRepository, customerRepository, userRepository,
                cashMovementService);

        User user = mock(User.class);
        when(user.getUserName()).thenReturn(new UserName("Test User"));
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(mock(Customer.class)));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
    }

    @Test
    void should_CreateRefund_When_CustomerHasRefundableBalance() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("50.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId)).thenReturn(new BigDecimal("100.00"));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request, userId);

        assertEquals(PaymentStatus.REFUND.name(), response.status());
        assertEquals(new BigDecimal("50.00"), response.amount());
        verify(paymentRepository).getRefundableAmountByCustomerId(customerId);
        verify(cashMovementService).record(
                CashMovementDirection.OUT,
                CashMovementType.REFUND,
                new BigDecimal("50.00"),
                paymentMethodId,
                "Devolucion por cualquier concepto",
                "PAYMENT",
                response.id(),
                userId,
                "Test User"
        );
    }

    @Test
    void should_RejectAppliedPaymentBeforeSaving_When_RegisterIsClosed() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.APPLIED);
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashMovementService).requireOpenRegister();

        assertThrows(IllegalStateException.class, () -> useCase.execute(request, userId));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void should_AllowPendingPaymentWithoutOpenRegister() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.PENDING);
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(request, userId);

        verify(cashMovementService, never()).requireOpenRegister();
        verify(cashMovementService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void should_RecordIncomeAfterSavingAppliedPayment() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.APPLIED);
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request, userId);

        verify(cashMovementService).requireOpenRegister();
        verify(cashMovementService).record(
                CashMovementDirection.IN,
                CashMovementType.PAYMENT,
                new BigDecimal("50.00"),
                paymentMethodId,
                "Pago de prueba",
                "PAYMENT",
                response.id(),
                userId,
                "Test User"
        );
    }

    @Test
    void should_PersistTargetInvoiceIdAndVoucherPath_When_Provided() {
        UUID targetInvoiceId = UUID.randomUUID();
        CreatePaymentRequest request = new CreatePaymentRequest(
                customerId,
                PaymentStatus.PENDING.name(),
                new BigDecimal("50.00"),
                paymentMethodId,
                "PAYMENT-001",
                LocalDate.now(),
                "Pago de prueba",
                targetInvoiceId,
                "payments/justificantes/abc.pdf"
        );
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request, userId);

        assertEquals(targetInvoiceId, response.targetInvoiceId());
        assertEquals("/api/files/payments/justificantes/abc.pdf", response.voucherUrl());
    }

    @Test
    void should_Throw_When_RefundCustomerHasNoAppliedPayments() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("50.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId)).thenReturn(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request, userId)
        );

        assertEquals("Cannot create a refund because the customer has no applied payments", exception.getMessage());
    }

    @Test
    void should_Throw_When_RefundExceedsRefundableBalance() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("150.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId)).thenReturn(new BigDecimal("100.00"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request, userId)
        );

        assertEquals("Refund amount cannot exceed customer's refundable balance", exception.getMessage());
    }

    private CreatePaymentRequest refundRequest(BigDecimal amount) {
        return new CreatePaymentRequest(
                customerId,
                PaymentStatus.REFUND.name(),
                amount,
                paymentMethodId,
                "REFUND-001",
                LocalDate.now(),
                "Devolucion por cualquier concepto",
                null,
                null
        );
    }

    private CreatePaymentRequest paymentRequest(PaymentStatus status) {
        return new CreatePaymentRequest(
                customerId,
                status.name(),
                new BigDecimal("50.00"),
                paymentMethodId,
                "PAYMENT-001",
                LocalDate.now(),
                "Pago de prueba",
                null,
                null
        );
    }
}
