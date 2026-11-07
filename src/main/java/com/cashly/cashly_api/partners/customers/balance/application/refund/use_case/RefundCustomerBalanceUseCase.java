package com.cashly.cashly_api.partners.customers.balance.application.refund.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;

import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway.CustomerCashMovement;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class RefundCustomerBalanceUseCase {

    private final ConsumeCustomerBalanceUseCase consumeBalance;
    private final CustomerCashGateway cashGateway;
    private final CustomerActorGateway actorGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public RefundCustomerBalanceUseCase(
            ConsumeCustomerBalanceUseCase consumeBalance,
            CustomerCashGateway cashGateway,
            CustomerActorGateway actorGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.consumeBalance = consumeBalance;
        this.cashGateway = cashGateway;
        this.actorGateway = actorGateway;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BigDecimal amount,
            UUID paymentMethodId, String notes, UUID userId) {
        cashGateway.requireOpenRegister();
        String userName = userName(userId);
        String description = notes == null || notes.isBlank()
                ? "Devolución de saldo a favor"
                : notes;
        var mutation = consumeBalance.executeWithTransaction(customerId,
                BalanceType.REFUND, amount, BalanceType.REFUND, null,
                description, null, userId);
        cashGateway.record(new CustomerCashMovement("OUT", "REFUND", amount,
                paymentMethodId, description, "CUSTOMER_BALANCE",
                mutation.transactionId(), userId, userName));
        activityNotifications.record(ActivityEvent.of("CONTACTS_CUSTOMERS",
                "BALANCE", "CUSTOMER", customerId,
                customerId.toString(), userId));
        return mutation.balance();
    }

    private String userName(UUID userId) {
        return actorGateway.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found: " + userId))
                .name();
    }
}
