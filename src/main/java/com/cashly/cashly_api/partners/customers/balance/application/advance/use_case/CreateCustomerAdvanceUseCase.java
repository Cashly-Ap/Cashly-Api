package com.cashly.cashly_api.partners.customers.balance.application.advance.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;

import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerActorGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.out.CustomerCashGateway.CustomerCashMovement;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CreateCustomerAdvanceUseCase {

    private final AddToCustomerBalanceUseCase addToBalance;
    private final CustomerCashGateway cashGateway;
    private final CustomerActorGateway actorGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public CreateCustomerAdvanceUseCase(
            AddToCustomerBalanceUseCase addToBalance,
            CustomerCashGateway cashGateway,
            CustomerActorGateway actorGateway,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.addToBalance = addToBalance;
        this.cashGateway = cashGateway;
        this.actorGateway = actorGateway;
    }

    @Transactional
    @Audited(module = "partners", entityType = "CUSTOMER", action = "ADVANCE")
    public CustomerBalanceResponse execute(UUID customerId, BigDecimal amount,
            UUID paymentMethodId, String notes, UUID userId) {
        cashGateway.requireOpenRegister();
        String userName = userName(userId);
        String description = notes == null || notes.isBlank()
                ? "Anticipo de cliente"
                : notes;
        var mutation = addToBalance.executeWithTransaction(customerId,
                BalanceType.ADVANCED, amount, BalanceType.ADVANCED, null,
                description, null, userId);
        cashGateway.record(new CustomerCashMovement("IN", "ADVANCE", amount,
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
