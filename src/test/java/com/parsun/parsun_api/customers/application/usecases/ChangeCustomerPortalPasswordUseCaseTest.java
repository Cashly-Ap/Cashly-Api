package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.ChangeCustomerPortalPasswordRequest;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeCustomerPortalPasswordUseCase Tests")
class ChangeCustomerPortalPasswordUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ChangeCustomerPortalPasswordUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ChangeCustomerPortalPasswordUseCase(customerRepository, passwordEncoder);
    }

    @Test
    @DisplayName("should change portal password successfully")
    void shouldChangePortalPasswordSuccessfully() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest("NewSecure@123");

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode("NewSecure@123")).thenReturn("encoded-new-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(customerId.toString(), request);

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(passwordEncoder).encode("NewSecure@123");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should accept customer rif as new password")
    void shouldAcceptCustomerRifAsPassword() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        String rif = customer.getRif().getValue();
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest(rif);

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode(rif)).thenReturn("encoded-rif-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(customerId.toString(), request);

        verify(passwordEncoder).encode(rif);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest("NewSecure@123");

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId.toString(), request));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for too short password")
    void shouldThrowForShortPassword() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest("Ab@123");

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(customerId.toString(), request));

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for password without uppercase letter")
    void shouldThrowForMissingUppercase() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest("abcde@123");

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(customerId.toString(), request));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for password without special character")
    void shouldThrowForMissingSpecialCharacter() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        ChangeCustomerPortalPasswordRequest request = new ChangeCustomerPortalPasswordRequest("Abcdef123");

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(customerId.toString(), request));
    }
}
