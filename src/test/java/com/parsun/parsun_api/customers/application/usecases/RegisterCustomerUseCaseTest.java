package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.AddressRequest;
import com.parsun.parsun_api.customers.application.dto.request.CreateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.request.EmailRequest;
import com.parsun.parsun_api.customers.application.dto.request.PhoneRequest;
import com.parsun.parsun_api.customers.application.dto.response.CustomerCreateResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.services.CustomerCodeGenerator;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCustomerUseCase Tests")
class RegisterCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerCodeGenerator codeGenerator;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterCustomerUseCase(customerRepository, codeGenerator, mapper, passwordEncoder);
    }

    @Test
    @DisplayName("should register customer successfully")
    void shouldRegisterCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(java.util.UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(1001L, response.code());
        assertEquals("Test Corp", response.legalName());
        assertTrue(response.active());
        verify(codeGenerator).generateNext();
        verify(customerRepository).save(any(Customer.class));
        verify(mapper).toCreateResponse(any(Customer.class));
    }

    @Test
    @DisplayName("should deactivate customer when active is false")
    void shouldDeactivateCustomerWhenActiveIsFalse() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, false,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(java.util.UUID.randomUUID());
        customer.deactivate();

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, false,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request);

        assertNotNull(response);
        assertFalse(response.active());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should enable portal access with encoded password when portalAccess is true")
    void shouldEnablePortalAccessWhenRequested() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                true, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(java.util.UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), true, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request);

        assertNotNull(response);
        assertTrue(response.portalAccess());
        verify(passwordEncoder).encode(anyString());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should add emails, phones, and addresses from request")
    void shouldAddEmailsPhonesAndAddresses() {
        List<EmailRequest> emails = List.of(new EmailRequest("test@example.com"));
        List<PhoneRequest> phones = List.of(new PhoneRequest("+58-1234567890"));
        List<AddressRequest> addresses = List.of(
                new AddressRequest("123 Main St", true, true, "State", "Municipality", "1000")
        );

        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                emails, phones, addresses,
                false, "Some notes", null
        );

        Customer customer = CustomerTestHelper.buildCustomer(java.util.UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, "Some notes", null
                )
        );

        CustomerCreateResponse response = useCase.execute(request);

        assertNotNull(response);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("should call code generator to generate customer code")
    void shouldCallCodeGenerator() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(java.util.UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(2002L));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 2002L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(2002L, response.code());
        verify(codeGenerator).generateNext();
    }
}
