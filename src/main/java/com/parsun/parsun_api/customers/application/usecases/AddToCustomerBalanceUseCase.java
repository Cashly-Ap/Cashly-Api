package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.application.mappers.BalanceMapper;
import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AddToCustomerBalanceUseCase {

    private final CustomerBalanceRepository customerBalanceRepository;

    public AddToCustomerBalanceUseCase(CustomerBalanceRepository customerBalanceRepository) {
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BalanceType type, BigDecimal amount,
                                            BalanceType referenceType, UUID referenceId,
                                            String notes, UUID createdBy) {
        CustomerBalance balance = customerBalanceRepository.findByCustomerId(customerId)
            .orElseGet(() -> CustomerBalance.create(customerId));

        balance.addToBalance(amount);

        BalanceTransaction transaction = BalanceTransaction.create(
            customerId,
            type,
            amount,
            referenceType,
            referenceId,
            notes,
            createdBy
        );

        customerBalanceRepository.save(balance);
        customerBalanceRepository.saveTransaction(transaction);

        return BalanceMapper.toResponse(balance);
    }
}
