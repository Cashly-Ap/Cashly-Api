package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCustomerUseCase Tests")
class DeleteCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    private DeleteCustomerUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeleteCustomerUseCase(customerRepository);
    }

    @Test
    @DisplayName("should delete customer when exists")
    void shouldDeleteCustomerWhenExists() {
        when(customerRepository.existsById(new CustomerId(customerId))).thenReturn(true);

        useCase.execute(customerId.toString());

        verify(customerRepository).existsById(new CustomerId(customerId));
        verify(customerRepository).delete(new CustomerId(customerId));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.existsById(new CustomerId(customerId))).thenReturn(false);

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId.toString()));

        verify(customerRepository).existsById(new CustomerId(customerId));
        verify(customerRepository, never()).delete(any(CustomerId.class));
    }
}
