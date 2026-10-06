package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.application.dto.request.AddressRequest;
import com.cashly.cashly_api.customers.application.dto.request.EmailRequest;
import com.cashly.cashly_api.customers.application.dto.request.PhoneRequest;
import com.cashly.cashly_api.customers.application.dto.request.UpdateCustomerRequest;
import com.cashly.cashly_api.customers.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.customers.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.domain.valueobjects.Rif;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCustomerUseCase Tests")
class UpdateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UpdateCustomerUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateCustomerUseCase(customerRepository, mapper, passwordEncoder);
    }

    @Test
    @DisplayName("should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated Corp", "BUSINESS", "J-98765432-1", "Updated",
                45, new BigDecimal("1000.00"), true,
                List.of(new EmailRequest("new@example.com")),
                List.of(new PhoneRequest("+58-9876543210")),
                List.of(new AddressRequest("456 New St", true, true, "State", "Mun", "2000")),
                false, "Updated notes", null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDetailResponse(any(Customer.class), anyBoolean())).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Updated Corp", "BUSINESS",
                        "J-98765432-1", "Updated", 45, new BigDecimal("1000.00"), true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, "Updated notes", null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString(), request);

        assertNotNull(response);
        assertEquals("Updated Corp", response.legalName());
        verify(customerRepository).findById(new CustomerId(customerId));
        verify(customerRepository).save(any(Customer.class));
        verify(mapper).toDetailResponse(any(Customer.class), anyBoolean());
    }

    @Test
    @DisplayName("should reject a RIF registered by another customer")
    void shouldRejectRifRegisteredByAnotherCustomer() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated Corp", "BUSINESS", "J-98765432-1", "Updated",
                45, new BigDecimal("1000.00"), true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(customerRepository.existsByRifAndIdNot(new Rif("J-98765432-1"), new CustomerId(customerId)))
                .thenReturn(true);

        assertThrows(DuplicateCustomerRifException.class,
                () -> useCase.execute(customerId.toString(), request));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated Corp", "BUSINESS", "J-98765432-1", "Updated",
                45, new BigDecimal("1000.00"), true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId.toString(), request));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("should enable portal access with encoded password when portalAccess is true")
    void shouldEnablePortalAccessWhenTrue() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                true, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pw");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDetailResponse(any(Customer.class), anyBoolean())).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), true, null, null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString(), request);

        assertNotNull(response);
        assertTrue(response.portalAccess());
        verify(passwordEncoder).encode("J-12345678-9");
    }

    @Test
    @DisplayName("should disable portal access when portalAccess is false")
    void shouldDisablePortalAccessWhenFalse() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId, true, true);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDetailResponse(any(Customer.class), anyBoolean())).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString(), request);

        assertNotNull(response);
        assertFalse(response.portalAccess());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should activate customer when active is true")
    void shouldActivateCustomerWhenActiveIsTrue() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId, false, false);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDetailResponse(any(Customer.class), anyBoolean())).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString(), request);

        assertNotNull(response);
        assertTrue(response.active());
    }

    @Test
    @DisplayName("should deactivate customer when active is false")
    void shouldDeactivateCustomerWhenActiveIsFalse() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId, true, false);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, false,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDetailResponse(any(Customer.class), anyBoolean())).thenReturn(
                new CustomerDetailResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, false,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null, true
                )
        );

        CustomerDetailResponse response = useCase.execute(customerId.toString(), request);

        assertNotNull(response);
        assertFalse(response.active());
    }
}
