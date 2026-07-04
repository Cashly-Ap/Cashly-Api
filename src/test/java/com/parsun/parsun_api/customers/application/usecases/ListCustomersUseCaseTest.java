package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerListResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCustomersUseCase Tests")
class ListCustomersUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    private ListCustomersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListCustomersUseCase(customerRepository, mapper);
    }

    @Test
    @DisplayName("should return paginated list with filters")
    void shouldReturnPaginatedListWithFilters() {
        UUID id = UUID.randomUUID();
        Customer customer = CustomerTestHelper.buildCustomer(id);
        CustomerListResponse listResponse = new CustomerListResponse(
                id.toString(), 1001L, "Test", "Test Corp", "J-12345678-9",
                "BUSINESS", true, PaymentType.CASH, false
        );

        PageResponse<Customer> page = new PageResponse<>(
                List.of(customer), 1L, 1, 10, 0, true, true, false
        );

        when(customerRepository.findAllWithFilters(
                eq("search"), eq("J-123"), eq("BUSINESS"), eq(true),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10), isNull(), isNull()
        )).thenReturn(page);
        when(mapper.toListResponse(customer)).thenReturn(listResponse);

        PageResponse<CustomerListResponse> response = useCase.execute(
                "search", "J-123", "BUSINESS", true, null, null, null, null, null, 0, 10, null, null
        );

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals(1L, response.totalElements());
        assertEquals("Test", response.content().get(0).alias());
        verify(customerRepository).findAllWithFilters(
                "search", "J-123", "BUSINESS", true, null, null, null, null,null, 0, 10, null, null
        );
    }

    @Test
    @DisplayName("should default active filter to true when null")
    void shouldDefaultActiveFilterToTrue() {
        PageResponse<Customer> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(customerRepository.findAllWithFilters(
            isNull(), isNull(), isNull(), eq(true),
            isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(0), eq(10), isNull(), isNull()
        )).thenReturn(page);

        PageResponse<CustomerListResponse> response = useCase.execute(
                null, null, null, null, null, null, null, null,null, 0, 10, null, null
        );

        assertNotNull(response);
        assertTrue(response.empty());
        verify(customerRepository).findAllWithFilters(
                null, null, null, true, null, null, null, null,null, 0, 10, null, null
        );
    }

    @Test
    @DisplayName("should return empty page when no results")
    void shouldReturnEmptyPageWhenNoResults() {
        PageResponse<Customer> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(customerRepository.findAllWithFilters(
            any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(),
            anyInt(), anyInt(), any(), any()
        )).thenReturn(page);

        PageResponse<CustomerListResponse> response = useCase.execute(
                null, null, null, true, null, null, null, null,null, 0, 10, null, null
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0L, response.totalElements());
        assertTrue(response.empty());
    }
}
