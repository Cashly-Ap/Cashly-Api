package com.cashly.cashly_api.receivables.application.usecases;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCustomerDebtUseCase Unit Tests")
class SearchCustomerDebtUseCaseTest {

    @Mock
    private ReceivablesQueryGateway receivablesRepository;

    private SearchCustomerDebtUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchCustomerDebtUseCase(receivablesRepository);
    }

    @Test
    void should_ReturnPaginatedResults_When_SearchIsProvided() {
        String search = "cliente";
        int page = 0;
        int size = 20;
        String sort = "totalDebt,desc";

        CustomerDebtListResponse customer = new CustomerDebtListResponse(
                UUID.randomUUID(), "Cliente Test", "J-12345678-9",
                new BigDecimal("5000.00"), new BigDecimal("1000.00"), 3L,
                LocalDate.now().minusMonths(2), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"), BigDecimal.ZERO
        );
        PageResponse<CustomerDebtListResponse> customerPage = toPageResponse(new PageImpl<>(List.of(customer)));

        when(receivablesRepository.findCustomersWithDebt(search, page, size, sort))
                .thenReturn(customerPage);

        PageResponse<CustomerDebtListResponse> result = useCase.execute(search, page, size, sort);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(1, result.content().size());
        assertEquals("Cliente Test", result.content().get(0).customerName());
        assertEquals(new BigDecimal("5000.00"), result.content().get(0).totalDebt());
        verify(receivablesRepository).findCustomersWithDebt(search, page, size, sort);
    }

    @Test
    void should_ReturnEmptyResults_When_NoCustomersMatch() {
        PageResponse<CustomerDebtListResponse> emptyPage = toPageResponse(org.springframework.data.domain.Page.empty());

        when(receivablesRepository.findCustomersWithDebt("nonexistent", 0, 20, null))
                .thenReturn(emptyPage);

        PageResponse<CustomerDebtListResponse> result = useCase.execute("nonexistent", 0, 20, null);

        assertTrue(result.empty());
        assertEquals(0, result.totalElements());
        verify(receivablesRepository).findCustomersWithDebt("nonexistent", 0, 20, null);
    }

    @Test
    void should_ReturnAllCustomers_When_SearchIsNull() {
        CustomerDebtListResponse customer1 = new CustomerDebtListResponse(
                UUID.randomUUID(), "Cliente A", "J-11111111-1",
                BigDecimal.TEN, BigDecimal.ZERO, 1L,
                LocalDate.now().minusMonths(1), BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.ZERO
        );
        CustomerDebtListResponse customer2 = new CustomerDebtListResponse(
                UUID.randomUUID(), "Cliente B", "J-22222222-2",
                BigDecimal.valueOf(20), BigDecimal.ZERO, 2L,
                LocalDate.now().minusWeeks(3), BigDecimal.valueOf(200), BigDecimal.valueOf(180), BigDecimal.ZERO
        );
        PageResponse<CustomerDebtListResponse> customerPage = toPageResponse(new PageImpl<>(List.of(customer1, customer2)));

        when(receivablesRepository.findCustomersWithDebt(null, 0, 20, null))
                .thenReturn(customerPage);

        PageResponse<CustomerDebtListResponse> result = useCase.execute(null, 0, 20, null);

        assertEquals(2, result.content().size());
        assertEquals("Cliente A", result.content().get(0).customerName());
        assertEquals("Cliente B", result.content().get(1).customerName());
        verify(receivablesRepository).findCustomersWithDebt(null, 0, 20, null);
    }

    @Test
    void should_ReturnCorrectPageInfo_When_PaginationUsed() {
        CustomerDebtListResponse customer = new CustomerDebtListResponse(
                UUID.randomUUID(), "Cliente Test", "J-12345678-9",
                new BigDecimal("5000.00"), new BigDecimal("1000.00"), 3L,
                LocalDate.now().minusMonths(2), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"), BigDecimal.ZERO
        );
        PageResponse<CustomerDebtListResponse> customerPage = toPageResponse(new PageImpl<>(
                List.of(customer), org.springframework.data.domain.PageRequest.of(1, 10), 25
        ));

        when(receivablesRepository.findCustomersWithDebt(null, 1, 10, null))
                .thenReturn(customerPage);

        PageResponse<CustomerDebtListResponse> result = useCase.execute(null, 1, 10, null);

        assertEquals(1, result.content().size());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(10, result.size());
        assertEquals(1, result.number());
        assertFalse(result.first());
        assertFalse(result.last());
    }
    private static <T> PageResponse<T> toPageResponse(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getSize(), page.getNumber(), page.isFirst(), page.isLast(), page.isEmpty());
    }
}
