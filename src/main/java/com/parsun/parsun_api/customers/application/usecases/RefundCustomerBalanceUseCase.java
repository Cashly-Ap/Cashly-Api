package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class RefundCustomerBalanceUseCase {

    private final ConsumeCustomerBalanceUseCase consumeBalance;
    private final CashMovementService cashMovementService;
    private final UserRepository userRepository;

    public RefundCustomerBalanceUseCase(ConsumeCustomerBalanceUseCase consumeBalance,
                                        CashMovementService cashMovementService,
                                        UserRepository userRepository) {
        this.consumeBalance = consumeBalance;
        this.cashMovementService = cashMovementService;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerBalanceResponse execute(UUID customerId, BigDecimal amount,
                                           UUID paymentMethodId, String notes, UUID userId) {
        cashMovementService.requireOpenRegister();
        String userName = userName(userId);
        String description = notes == null || notes.isBlank() ? "Devolución de saldo a favor" : notes;
        var mutation = consumeBalance.executeWithTransaction(
                customerId, BalanceType.REFUND, amount, BalanceType.REFUND,
                null, description, null, userId);
        cashMovementService.record(
                CashMovementDirection.OUT, CashMovementType.REFUND, amount,
                paymentMethodId, description, "CUSTOMER_BALANCE", mutation.transactionId(),
                userId, userName);
        return mutation.balance();
    }

    private String userName(UUID userId) {
        return userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId))
                .getUserName().getValue();
    }
}
