package com.cashly.cashly_api.partners.customers.balance.application.add.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceMutationResponse;
import com.cashly.cashly_api.partners.customers.balance.application.mappers.BalanceMapper;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AddToCustomerBalanceUseCase {

    private final CustomerBalanceRepository customerBalanceRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public AddToCustomerBalanceUseCase(
            CustomerBalanceRepository customerBalanceRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BalanceType type,
            BigDecimal amount, BalanceType referenceType, UUID referenceId,
            String number, String referenceNumber, UUID createdBy) {
        var result = executeWithTransaction(customerId, type, amount,
                referenceType, referenceId, number, referenceNumber, createdBy);
        activityNotifications.record(ActivityEvent.of("CONTACTS_CUSTOMERS",
                "BALANCE", "CUSTOMER", customerId,
                customerId.toString(), createdBy));
        return result.balance();
    }

    @Transactional
    public CustomerBalanceMutationResponse executeWithTransaction(
            UUID customerId, BalanceType type, BigDecimal amount,
            BalanceType referenceType, UUID referenceId, String number,
            String referenceNumber, UUID createdBy) {
        CustomerBalance balance = customerBalanceRepository
                .findByCustomerId(customerId)
                .orElseGet(() -> CustomerBalance.create(customerId));

        balance.addToBalance(amount);

        BalanceTransaction transaction = BalanceTransaction.create(customerId,
                type, amount, referenceType, referenceId, number,
                referenceNumber, createdBy);

        customerBalanceRepository.save(balance);
        customerBalanceRepository.saveTransaction(transaction);

        return new CustomerBalanceMutationResponse(
                BalanceMapper.toResponse(balance), transaction.getId());
    }
}
