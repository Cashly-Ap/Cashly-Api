package com.cashly.cashly_api.partners.customers.balance.application.add.use_case;

import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddToCustomerBalanceUseCase Tests")
class AddToCustomerBalanceUseCaseTest {

    @Mock
    private CustomerBalanceRepository customerBalanceRepository;

    private AddToCustomerBalanceUseCase useCase;

    @Captor
    private ArgumentCaptor<CustomerBalance> balanceCaptor;

    @Captor
    private ArgumentCaptor<BalanceTransaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        useCase = new AddToCustomerBalanceUseCase(customerBalanceRepository);
    }

    @Test
    @DisplayName("should add to existing balance successfully")
    void should_add_to_existing_balance_successfully() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("300.00");
        BigDecimal addAmount = new BigDecimal("200.00");

        CustomerBalance balance = CustomerBalance.reconstitute(
                UUID.randomUUID(), customerId, initialBalance,
                LocalDateTime.now(), LocalDateTime.now());

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.of(balance));
        when(customerBalanceRepository.save(any(CustomerBalance.class)))
                .thenReturn(balance);
        when(customerBalanceRepository
                .saveTransaction(any(BalanceTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CustomerBalanceResponse response = useCase.execute(customerId,
                BalanceType.ADVANCED, addAmount, BalanceType.ADVANCED, null,
                null, null, createdBy);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.totalBalance());
        assertEquals(customerId, response.customerId());

        verify(customerBalanceRepository).save(balanceCaptor.capture());
        CustomerBalance savedBalance = balanceCaptor.getValue();
        assertEquals(new BigDecimal("500.00"), savedBalance.getTotalBalance());

        verify(customerBalanceRepository)
                .saveTransaction(transactionCaptor.capture());
        BalanceTransaction savedTx = transactionCaptor.getValue();
        assertEquals(BalanceType.ADVANCED, savedTx.getType());
        assertEquals(addAmount, savedTx.getAmount());
        assertEquals(createdBy, savedTx.getCreatedBy());
    }

    @Test
    @DisplayName("should create new balance when customer has no balance")
    void should_create_new_balance_when_customer_has_no_balance() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.empty());
        when(customerBalanceRepository.save(any(CustomerBalance.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(customerBalanceRepository
                .saveTransaction(any(BalanceTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CustomerBalanceResponse response = useCase.execute(customerId,
                BalanceType.ADVANCED, amount, BalanceType.ADVANCED, null, null,
                null, createdBy);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
        assertEquals(amount, response.totalBalance());

        verify(customerBalanceRepository).findByCustomerId(customerId);
        verify(customerBalanceRepository).save(balanceCaptor.capture());
        CustomerBalance savedBalance = balanceCaptor.getValue();
        assertEquals(customerId, savedBalance.getCustomerId());
        assertEquals(amount, savedBalance.getTotalBalance());

        verify(customerBalanceRepository)
                .saveTransaction(transactionCaptor.capture());
        BalanceTransaction savedTx = transactionCaptor.getValue();
        assertEquals(BalanceType.ADVANCED, savedTx.getType());
        assertEquals(amount, savedTx.getAmount());
        assertEquals(createdBy, savedTx.getCreatedBy());
    }

    @Test
    @DisplayName("should throw when adding negative amount")
    void should_throw_when_adding_negative_amount() {
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("-100.00");

        CustomerBalance balance = CustomerBalance.reconstitute(
                UUID.randomUUID(), customerId, BigDecimal.ZERO,
                LocalDateTime.now(), LocalDateTime.now());

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.of(balance));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(customerId, BalanceType.ADVANCED, amount,
                        BalanceType.ADVANCED, null, null, null,
                        UUID.randomUUID()));

        verify(customerBalanceRepository).findByCustomerId(customerId);
        verify(customerBalanceRepository, never()).save(any());
        verify(customerBalanceRepository, never()).saveTransaction(any());
    }
}
