package com.cashly.cashly_api.finance.treasury.payments.collection.application.create.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentActorGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.create.request.CreatePaymentRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
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
    private PaymentCustomerGateway customerRepository;

    @Mock
    private PaymentActorGateway userRepository;

    @Mock
    private CashRegisterGateway cashMovementService;

    private CreatePaymentUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreatePaymentUseCase(paymentRepository,
                customerRepository, userRepository, cashMovementService);

        when(customerRepository.exists(customerId)).thenReturn(true);
        when(userRepository.findName(userId))
                .thenReturn(Optional.of("Test User"));
    }

    @Test
    void should_CreateRefund_When_CustomerHasRefundableBalance() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("50.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId))
                .thenReturn(new BigDecimal("100.00"));
        when(paymentRepository
                .getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request, userId);

        assertEquals(PaymentStatus.REFUND.name(), response.status());
        assertEquals(new BigDecimal("50.00"), response.amount());
        verify(paymentRepository).getRefundableAmountByCustomerId(customerId);
        verify(cashMovementService)
                .recordMovement(new CashRegisterGateway.Movement(
                        CashRegisterGateway.Direction.OUT,
                        CashRegisterGateway.Type.REFUND,
                        new CashRegisterGateway.PaymentEntry(
                                new BigDecimal("50.00"), paymentMethodId,
                                "Devolucion por cualquier concepto",
                                response.id(), userId, "Test User")));
    }

    @Test
    void should_RejectAppliedPaymentBeforeSaving_When_RegisterIsClosed() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.APPLIED);
        doThrow(new IllegalStateException(
                "Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashMovementService).requireOpenRegister();

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(request, userId));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void should_AllowPendingPaymentWithoutOpenRegister() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.PENDING);
        when(paymentRepository
                .getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(request, userId);

        verify(cashMovementService, never()).requireOpenRegister();
        verify(cashMovementService, never()).recordMovement(any());
    }

    @Test
    void should_RecordIncomeAfterSavingAppliedPayment() {
        CreatePaymentRequest request = paymentRequest(PaymentStatus.APPLIED);
        when(paymentRepository
                .getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request, userId);

        verify(cashMovementService).requireOpenRegister();
        verify(cashMovementService)
                .recordMovement(new CashRegisterGateway.Movement(
                        CashRegisterGateway.Direction.IN,
                        CashRegisterGateway.Type.PAYMENT,
                        new CashRegisterGateway.PaymentEntry(
                                new BigDecimal("50.00"), paymentMethodId,
                                "Pago de prueba", response.id(), userId,
                                "Test User")));
    }

    @Test
    void should_Throw_When_RefundCustomerHasNoAppliedPayments() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("50.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId))
                .thenReturn(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request, userId));

        assertEquals(
                "Cannot create a refund because the customer has no applied payments",
                exception.getMessage());
    }

    @Test
    void should_Throw_When_RefundExceedsRefundableBalance() {
        CreatePaymentRequest request = refundRequest(new BigDecimal("150.00"));
        when(paymentRepository.getRefundableAmountByCustomerId(customerId))
                .thenReturn(new BigDecimal("100.00"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request, userId));

        assertEquals(
                "Refund amount cannot exceed customer's refundable balance",
                exception.getMessage());
    }

    private CreatePaymentRequest refundRequest(BigDecimal amount) {
        return new CreatePaymentRequest(customerId, PaymentStatus.REFUND.name(),
                amount, paymentMethodId, "REFUND-001", LocalDate.now(),
                "Devolucion por cualquier concepto");
    }

    private CreatePaymentRequest paymentRequest(PaymentStatus status) {
        return new CreatePaymentRequest(customerId, status.name(),
                new BigDecimal("50.00"), paymentMethodId, "PAYMENT-001",
                LocalDate.now(), "Pago de prueba");
    }
}
