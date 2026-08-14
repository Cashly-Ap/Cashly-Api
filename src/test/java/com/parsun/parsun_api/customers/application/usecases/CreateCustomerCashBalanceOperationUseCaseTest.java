package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceMutationResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerCashBalanceOperationUseCaseTest {

    @Mock private AddToCustomerBalanceUseCase addToBalance;
    @Mock private ConsumeCustomerBalanceUseCase consumeBalance;
    @Mock private CashMovementService cashMovementService;
    @Mock private UserRepository userRepository;

    private CreateCustomerAdvanceUseCase advanceUseCase;
    private RefundCustomerBalanceUseCase refundUseCase;
    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        advanceUseCase = new CreateCustomerAdvanceUseCase(addToBalance, cashMovementService, userRepository);
        refundUseCase = new RefundCustomerBalanceUseCase(consumeBalance, cashMovementService, userRepository);
    }

    @Test
    void should_RejectAdvanceBeforeChangingBalance_When_RegisterIsClosed() {
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashMovementService).requireOpenRegister();

        assertThrows(IllegalStateException.class, () -> advanceUseCase.execute(
                customerId, new BigDecimal("100.00"), paymentMethodId, "Anticipo", userId));

        verify(addToBalance, never()).executeWithTransaction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void should_RecordAdvanceIncomeLinkedToBalanceTransaction() {
        CustomerBalanceResponse balance = new CustomerBalanceResponse(customerId, new BigDecimal("100.00"), null, null);
        when(addToBalance.executeWithTransaction(customerId, BalanceType.ADVANCED, new BigDecimal("100.00"),
                BalanceType.ADVANCED, null, "Anticipo", null, userId))
                .thenReturn(new CustomerBalanceMutationResponse(balance, transactionId));
        mockUser();

        var result = advanceUseCase.execute(customerId, new BigDecimal("100.00"), paymentMethodId, "Anticipo", userId);

        assertSame(balance, result);
        verify(cashMovementService).record(CashMovementDirection.IN, CashMovementType.ADVANCE,
                new BigDecimal("100.00"), paymentMethodId, "Anticipo",
                "CUSTOMER_BALANCE", transactionId, userId, "Operator");
    }

    @Test
    void should_RecordRefundOutputLinkedToBalanceTransaction() {
        CustomerBalanceResponse balance = new CustomerBalanceResponse(customerId, BigDecimal.ZERO, null, null);
        when(consumeBalance.executeWithTransaction(customerId, BalanceType.REFUND, new BigDecimal("75.00"),
                BalanceType.REFUND, null, "Devolución", null, userId))
                .thenReturn(new CustomerBalanceMutationResponse(balance, transactionId));
        mockUser();

        var result = refundUseCase.execute(customerId, new BigDecimal("75.00"), paymentMethodId, "Devolución", userId);

        assertSame(balance, result);
        verify(cashMovementService).record(CashMovementDirection.OUT, CashMovementType.REFUND,
                new BigDecimal("75.00"), paymentMethodId, "Devolución",
                "CUSTOMER_BALANCE", transactionId, userId, "Operator");
    }

    private void mockUser() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getUserName()).thenReturn(new UserName("Operator"));
        when(userRepository.findById(new UserId(userId))).thenReturn(Optional.of(user));
    }
}
