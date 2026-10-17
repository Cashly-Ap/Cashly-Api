package com.cashly.cashly_api.partners.customers.balance.application.advance.use_case;

import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;

import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway.CustomerCashMovement;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateCustomerAdvanceUseCase {

    private final AddToCustomerBalanceUseCase addToBalance;
    private final CustomerCashGateway cashGateway;
    private final CustomerActorGateway actorGateway;

    public CreateCustomerAdvanceUseCase(AddToCustomerBalanceUseCase addToBalance,
                                        CustomerCashGateway cashGateway,
                                        CustomerActorGateway actorGateway) {
        this.addToBalance = addToBalance;
        this.cashGateway = cashGateway;
        this.actorGateway = actorGateway;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BigDecimal amount,
                                           UUID paymentMethodId, String notes, UUID userId) {
        cashGateway.requireOpenRegister();
        String userName = userName(userId);
        String description = notes == null || notes.isBlank() ? "Anticipo de cliente" : notes;
        var mutation = addToBalance.executeWithTransaction(
                customerId, BalanceType.ADVANCED, amount, BalanceType.ADVANCED,
                null, description, null, userId);
        cashGateway.record(new CustomerCashMovement(
                "IN", "ADVANCE", amount, paymentMethodId, description,
                "CUSTOMER_BALANCE", mutation.transactionId(), userId, userName));
        return mutation.balance();
    }

    private String userName(UUID userId) {
        return actorGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId))
                .name();
    }
}
