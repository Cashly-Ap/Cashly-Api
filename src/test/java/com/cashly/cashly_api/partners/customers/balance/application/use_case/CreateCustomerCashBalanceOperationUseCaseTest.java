package com.cashly.cashly_api.partners.customers.balance.application.use_case;

import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.advance.use_case.CreateCustomerAdvanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.refund.use_case.RefundCustomerBalanceUseCase;

import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerActorGateway.CustomerActor;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway.CustomerCashMovement;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceMutationResponse;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    @Mock private CustomerCashGateway cashGateway;
    @Mock private CustomerActorGateway actorGateway;

    private CreateCustomerAdvanceUseCase advanceUseCase;
    private RefundCustomerBalanceUseCase refundUseCase;
    private final UUID customerId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        advanceUseCase = new CreateCustomerAdvanceUseCase(addToBalance, cashGateway, actorGateway);
        refundUseCase = new RefundCustomerBalanceUseCase(consumeBalance, cashGateway, actorGateway);
    }

    @Test
    void should_RejectAdvanceBeforeChangingBalance_When_RegisterIsClosed() {
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashGateway).requireOpenRegister();

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
        verify(cashGateway).record(new CustomerCashMovement("IN", "ADVANCE", new BigDecimal("100.00"),
                paymentMethodId, "Anticipo", "CUSTOMER_BALANCE", transactionId, userId, "Operator"));
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
        verify(cashGateway).record(new CustomerCashMovement("OUT", "REFUND", new BigDecimal("75.00"),
                paymentMethodId, "Devolución", "CUSTOMER_BALANCE", transactionId, userId, "Operator"));
    }

    private void mockUser() {
        when(actorGateway.findById(userId)).thenReturn(java.util.Optional.of(new CustomerActor(userId, "Operator")));
    }
}
