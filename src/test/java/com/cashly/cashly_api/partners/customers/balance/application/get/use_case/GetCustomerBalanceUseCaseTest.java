package com.cashly.cashly_api.partners.customers.balance.application.get.use_case;

import com.cashly.cashly_api.partners.customers.balance.application.dto.response.BalanceTransactionResponse;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerBalanceUseCase Tests")
class GetCustomerBalanceUseCaseTest {

    @Mock
    private CustomerBalanceRepository customerBalanceRepository;

    private GetCustomerBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerBalanceUseCase(customerBalanceRepository);
    }

    @Test
    @DisplayName("should get balance when customer has balance")
    void should_get_balance_when_customer_has_balance() {
        UUID customerId = UUID.randomUUID();
        CustomerBalance balance = CustomerBalance.reconstitute(
                UUID.randomUUID(), customerId, new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now());

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.of(balance));

        CustomerBalanceResponse response = useCase.execute(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
        assertEquals(new BigDecimal("500.00"), response.totalBalance());
        verify(customerBalanceRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("should return zero balance when customer has no balance")
    void should_return_zero_balance_when_customer_has_no_balance() {
        UUID customerId = UUID.randomUUID();

        when(customerBalanceRepository.findByCustomerId(customerId))
                .thenReturn(Optional.empty());

        CustomerBalanceResponse response = useCase.execute(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
        assertEquals(BigDecimal.ZERO, response.totalBalance());
        assertNull(response.createdAt());
        assertNull(response.updatedAt());
        verify(customerBalanceRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("should get transactions page")
    void should_get_transactions_page() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        BalanceTransaction tx = BalanceTransaction.create(customerId,
                BalanceType.ADVANCED, BigDecimal.TEN, BalanceType.ADVANCED,
                null, null, null, createdBy);

        Page<BalanceTransaction> page = new PageImpl<>(List.of(tx));

        when(customerBalanceRepository.findTransactionsByCustomerId(
                eq(customerId), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(PageResponseMapper.from(page));

        PageResponse<BalanceTransactionResponse> response = useCase
                .getTransactions(customerId, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals(customerId, response.content().getFirst().customerId());
        verify(customerBalanceRepository).findTransactionsByCustomerId(
                eq(customerId), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("should get empty transactions page for customer without transactions")
    void should_get_empty_transactions_page_for_customer_without_transactions() {
        UUID customerId = UUID.randomUUID();

        Page<BalanceTransaction> emptyPage = Page.empty();

        when(customerBalanceRepository.findTransactionsByCustomerId(
                eq(customerId), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(PageResponseMapper.from(emptyPage));

        PageResponse<BalanceTransactionResponse> response = useCase
                .getTransactions(customerId, 0, 10);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        verify(customerBalanceRepository).findTransactionsByCustomerId(
                eq(customerId), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }
}
