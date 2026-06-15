package com.parsun.parsun_api.customers.infrastructure.web;

import com.parsun.parsun_api.customers.application.dto.request.CreateAdvanceRequest;
import com.parsun.parsun_api.customers.application.dto.request.CreateRefundRequest;
import com.parsun.parsun_api.customers.application.dto.response.BalanceTransactionResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.application.usecases.GetCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerBalanceController Unit Tests")
class CustomerBalanceControllerTest {

    @Mock
    private GetCustomerBalanceUseCase getCustomerBalanceUseCase;

    @Mock
    private AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;

    @Mock
    private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    @InjectMocks
    private CustomerBalanceController customerBalanceController;

    @Test
    @DisplayName("should get balance and return 200")
    void should_get_balance_and_return_200() {
        UUID customerId = UUID.randomUUID();
        CustomerBalanceResponse response = new CustomerBalanceResponse(
                customerId, BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now());

        when(getCustomerBalanceUseCase.execute(customerId)).thenReturn(response);

        ResponseEntity<CustomerBalanceResponse> result = customerBalanceController.getBalance(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(customerId, result.getBody().customerId());
        assertEquals(BigDecimal.TEN, result.getBody().totalBalance());
        verify(getCustomerBalanceUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should get zero balance when customer has no balance")
    void should_get_zero_balance_when_customer_has_no_balance() {
        UUID customerId = UUID.randomUUID();
        CustomerBalanceResponse response = new CustomerBalanceResponse(
                customerId, BigDecimal.ZERO, null, null);

        when(getCustomerBalanceUseCase.execute(customerId)).thenReturn(response);

        ResponseEntity<CustomerBalanceResponse> result = customerBalanceController.getBalance(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BigDecimal.ZERO, result.getBody().totalBalance());
        verify(getCustomerBalanceUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should get transactions and return 200")
    void should_get_transactions_and_return_200() {
        UUID customerId = UUID.randomUUID();
        BalanceTransactionResponse tx = new BalanceTransactionResponse(
                UUID.randomUUID(), customerId, "ADVANCED", BigDecimal.TEN,
                "ADVANCED", null, null, null, UUID.randomUUID(), LocalDateTime.now());

        PageResponse<BalanceTransactionResponse> pageResponse = new PageResponse<>(
                List.of(tx), 1L, 1, 10, 0, true, true, false);

        when(getCustomerBalanceUseCase.getTransactions(customerId, 0, 10)).thenReturn(pageResponse);

        ResponseEntity<PageResponse<BalanceTransactionResponse>> result =
                customerBalanceController.getTransactions(customerId, 0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        verify(getCustomerBalanceUseCase).getTransactions(customerId, 0, 10);
    }

    @Test
    @DisplayName("should get empty transactions list")
    void should_get_empty_transactions_list() {
        UUID customerId = UUID.randomUUID();

        PageResponse<BalanceTransactionResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(getCustomerBalanceUseCase.getTransactions(customerId, 0, 10)).thenReturn(emptyPage);

        ResponseEntity<PageResponse<BalanceTransactionResponse>> result =
                customerBalanceController.getTransactions(customerId, 0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
        verify(getCustomerBalanceUseCase).getTransactions(customerId, 0, 10);
    }

    @Test
    @DisplayName("should create advance and return 201")
    void should_create_advance_and_return_201() {
        UUID customerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        CustomerBalanceResponse response = new CustomerBalanceResponse(
                customerId, amount, LocalDateTime.now(), LocalDateTime.now());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userId.toString());

        when(addToCustomerBalanceUseCase.execute(
                eq(customerId), eq(BalanceType.ADVANCED), eq(amount),
                eq(BalanceType.ADVANCED), isNull(), isNull(), isNull(), eq(userId)))
                .thenReturn(response);

        CreateAdvanceRequest request = new CreateAdvanceRequest(amount, null);

        ResponseEntity<CustomerBalanceResponse> result =
                customerBalanceController.createAdvance(customerId, request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(customerId, result.getBody().customerId());
        assertEquals(amount, result.getBody().totalBalance());
        verify(addToCustomerBalanceUseCase).execute(
                eq(customerId), eq(BalanceType.ADVANCED), eq(amount),
                eq(BalanceType.ADVANCED), isNull(), isNull(), isNull(), eq(userId));
    }

    @Test
    @DisplayName("should create refund and return 201")
    void should_create_refund_and_return_201() {
        UUID customerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");

        CustomerBalanceResponse response = new CustomerBalanceResponse(
                customerId, new BigDecimal("300.00"), LocalDateTime.now(), LocalDateTime.now());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userId.toString());

        when(consumeCustomerBalanceUseCase.execute(
                eq(customerId), eq(BalanceType.REFUND), eq(amount),
                eq(BalanceType.REFUND), isNull(), isNull(), isNull(), eq(userId)))
                .thenReturn(response);

        CreateRefundRequest request = new CreateRefundRequest(amount);

        ResponseEntity<CustomerBalanceResponse> result =
                customerBalanceController.createRefund(customerId, request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(customerId, result.getBody().customerId());
        assertEquals(new BigDecimal("300.00"), result.getBody().totalBalance());
        verify(consumeCustomerBalanceUseCase).execute(
                eq(customerId), eq(BalanceType.REFUND), eq(amount),
                eq(BalanceType.REFUND), isNull(), isNull(), isNull(), eq(userId));
    }
}
