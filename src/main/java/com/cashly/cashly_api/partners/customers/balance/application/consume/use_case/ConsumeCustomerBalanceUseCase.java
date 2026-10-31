package com.cashly.cashly_api.partners.customers.balance.application.consume.use_case;

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

public class ConsumeCustomerBalanceUseCase {

    private final CustomerBalanceRepository customerBalanceRepository;

    public ConsumeCustomerBalanceUseCase(
            CustomerBalanceRepository customerBalanceRepository) {
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BalanceType type,
            BigDecimal amount, BalanceType referenceType, UUID referenceId,
            String number, String referenceNumber, UUID createdBy) {
        return executeWithTransaction(customerId, type, amount, referenceType,
                referenceId, number, referenceNumber, createdBy).balance();
    }

    @Transactional
    public CustomerBalanceMutationResponse executeWithTransaction(
            UUID customerId, BalanceType type, BigDecimal amount,
            BalanceType referenceType, UUID referenceId, String number,
            String referenceNumber, UUID createdBy) {
        CustomerBalance balance = customerBalanceRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Customer balance not found for customer id: "
                                + customerId));

        balance.consumeFromBalance(amount);

        BalanceTransaction transaction = BalanceTransaction.create(customerId,
                type, amount.negate(), referenceType, referenceId, number,
                referenceNumber, createdBy);

        customerBalanceRepository.save(balance);
        customerBalanceRepository.saveTransaction(transaction);

        return new CustomerBalanceMutationResponse(
                BalanceMapper.toResponse(balance), transaction.getId());
    }
}
