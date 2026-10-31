package com.cashly.cashly_api.partners.customers.balance.application.consume.use_case;

import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.application.mappers.BalanceMapper;
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
@DisplayName("ConsumeCustomerBalanceUseCase Tests")
class ConsumeCustomerBalanceUseCaseTest {

    @Mock
    private CustomerBalanceRepository customerBalanceRepository;

    private ConsumeCustomerBalanceUseCase useCase;

    @Captor
    private ArgumentCaptor<CustomerBalance> balanceCaptor;

    @Captor
    private ArgumentCaptor<BalanceTransaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        useCase = new ConsumeCustomerBalanceUseCase(customerBalanceRepository);
    }

    @Test
    @DisplayName("should consume from balance successfully")
    void should_consume_from_balance_successfully() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("500.00");
        BigDecimal consumeAmount = new BigDecimal("200.00");

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
                BalanceType.REFUND, consumeAmount, BalanceType.REFUND, null,
                null, null, createdBy);

        assertNotNull(response);
        assertEquals(new BigDecimal("300.00"), response.totalBalance());
        assertEquals(customerId, response.customerId());

        verify(customerBalanceRepository).save(balanceCaptor.capture());
        CustomerBalance savedBalance = balanceCaptor.getValue();
        assertEquals(new BigDecimal("300.00"), savedBalance.getTotalBalance());

        verify(customerBalanceRepository)
                .saveTransaction(transactionCaptor.capture());
        BalanceTransaction savedTx = transactionCaptor.getValue();
        assertEquals(BalanceType.REFUND, savedTx.getType());
        assertEquals(consumeAmount.negate(), savedTx.getAmount());
        assertEquals(createdBy, savedTx.getCreatedBy());
    }

    @Test
    @DisplayName("should throw when customer balance not found")
    void should_throw_when_customer_balance_not_found() {
        UUID customerId = UUID.randomUUID();

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(customerId, BalanceType.REFUND,
                        BigDecimal.TEN, BalanceType.REFUND, null, null, null,
                        UUID.randomUUID()));

        assertTrue(exception.getMessage().contains(customerId.toString()));
        verify(customerBalanceRepository).findByCustomerId(customerId);
        verify(customerBalanceRepository, never()).save(any());
        verify(customerBalanceRepository, never()).saveTransaction(any());
    }

    @Test
    @DisplayName("should throw when insufficient balance")
    void should_throw_when_insufficient_balance() {
        UUID customerId = UUID.randomUUID();

        CustomerBalance balance = CustomerBalance.reconstitute(
                UUID.randomUUID(), customerId, new BigDecimal("50.00"),
                LocalDateTime.now(), LocalDateTime.now());

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.of(balance));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(customerId, BalanceType.REFUND,
                        new BigDecimal("100.00"), BalanceType.REFUND, null,
                        null, null, UUID.randomUUID()));

        assertTrue(exception.getMessage()
                .contains("Amount exceeds current balance"));
        verify(customerBalanceRepository).findByCustomerId(customerId);
        verify(customerBalanceRepository, never()).save(any());
        verify(customerBalanceRepository, never()).saveTransaction(any());
    }
}
