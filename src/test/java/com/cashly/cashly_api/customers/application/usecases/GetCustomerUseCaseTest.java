package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerUseCase Tests")
class GetCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    private GetCustomerUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerUseCase(customerRepository, mapper);
    }

    @Test
    @DisplayName("should return customer detail when found")
    void shouldReturnCustomerDetailWhenFound() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(mapper.toDetailResponse(customer, true)).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, java.math.BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString());

        assertNotNull(response);
        assertEquals(customerId.toString(), response.id());
        assertEquals("Test Corp", response.legalName());
        assertEquals(1001L, response.code());
        verify(customerRepository).findById(new CustomerId(customerId));
        verify(mapper).toDetailResponse(customer, true);
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId.toString()));

        verify(customerRepository).findById(new CustomerId(customerId));
    }
}
