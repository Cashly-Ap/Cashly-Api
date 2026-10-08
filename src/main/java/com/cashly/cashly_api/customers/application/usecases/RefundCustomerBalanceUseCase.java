package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.contracts.CustomerActorGateway;
import com.cashly.cashly_api.customers.application.contracts.CustomerCashGateway;
import com.cashly.cashly_api.customers.application.contracts.CustomerCashGateway.CustomerCashMovement;
import com.cashly.cashly_api.customers.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class RefundCustomerBalanceUseCase {

    private final ConsumeCustomerBalanceUseCase consumeBalance;
    private final CustomerCashGateway cashGateway;
    private final CustomerActorGateway actorGateway;

    public RefundCustomerBalanceUseCase(ConsumeCustomerBalanceUseCase consumeBalance,
                                        CustomerCashGateway cashGateway,
                                        CustomerActorGateway actorGateway) {
        this.consumeBalance = consumeBalance;
        this.cashGateway = cashGateway;
        this.actorGateway = actorGateway;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BigDecimal amount,
                                           UUID paymentMethodId, String notes, UUID userId) {
        cashGateway.requireOpenRegister();
        String userName = userName(userId);
        String description = notes == null || notes.isBlank() ? "Devolución de saldo a favor" : notes;
        var mutation = consumeBalance.executeWithTransaction(
                customerId, BalanceType.REFUND, amount, BalanceType.REFUND,
                null, description, null, userId);
        cashGateway.record(new CustomerCashMovement(
                "OUT", "REFUND", amount, paymentMethodId, description,
                "CUSTOMER_BALANCE", mutation.transactionId(), userId, userName));
        return mutation.balance();
    }

    private String userName(UUID userId) {
        return actorGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId))
                .name();
    }
}
