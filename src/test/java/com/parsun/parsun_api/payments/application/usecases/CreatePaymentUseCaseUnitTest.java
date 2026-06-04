package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
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

    private CreatePaymentUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreatePaymentUseCase(paymentRepository, customerRepository, userRepository);

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
                "Devolucion por cualquier concepto"
        );
    }
}
