package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.request.AddressRequest;
import com.parsun.parsun_api.customers.application.dto.request.CreateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.request.EmailRequest;
import com.parsun.parsun_api.customers.application.dto.request.PhoneRequest;
import com.parsun.parsun_api.customers.application.dto.response.CustomerCreateResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.application.exceptions.DuplicateCustomerRifException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.services.CustomerCodeGenerator;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerCode;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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

    @Mock
    private CustomerWarehouseAccessRepository warehouseAccessRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private RegisterCustomerUseCase useCase;

    private static final UUID GRANTED_BY = UUID.randomUUID();
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegisterCustomerUseCase(customerRepository, codeGenerator, mapper, passwordEncoder,
                warehouseAccessRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should register customer successfully")
    void shouldRegisterCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                List.of(WAREHOUSE_ID),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(WAREHOUSE_ID)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

        assertNotNull(response);
        assertEquals(1001L, response.code());
        assertEquals("Test Corp", response.legalName());
        assertTrue(response.active());
        verify(codeGenerator).generateNext();
        verify(customerRepository).save(any(Customer.class));
        verify(mapper).toCreateResponse(any(Customer.class));
    }

    @Test
    @DisplayName("should reject a customer when the RIF is already registered")
    void shouldRejectDuplicateRif() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Duplicate Corp", "BUSINESS", "J-12345678-9", "Duplicate",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                List.of(WAREHOUSE_ID),
                false, null, null
        );

        when(customerRepository.existsByRif(new Rif("J-12345678-9"))).thenReturn(true);

        assertThrows(DuplicateCustomerRifException.class,
                () -> useCase.execute(request, GRANTED_BY));

        verify(customerRepository, never()).save(any());
        verifyNoInteractions(codeGenerator, warehouseRepository, warehouseAccessRepository);
    }

    @Test
    @DisplayName("should grant warehouse access for each warehouse id")
    void shouldGrantWarehouseAccessForEachWarehouse() {
        UUID warehouseOne = UUID.randomUUID();
        UUID warehouseTwo = UUID.randomUUID();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                List.of(warehouseOne, warehouseTwo),
                false, null, null
        );

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.findById(warehouseOne)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(warehouseOne)));
        when(warehouseRepository.findById(warehouseTwo)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(warehouseTwo)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(new CustomerCreateResponse(
                UUID.randomUUID().toString(), 1001L, "Test Corp", "BUSINESS",
                "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                null, null, List.of(), List.of(), List.of(), false, null, null
        ));

        useCase.execute(request, GRANTED_BY);

        verify(warehouseAccessRepository, times(2)).save(any(CustomerWarehouseAccess.class));
        verify(warehouseRepository).findById(warehouseOne);
        verify(warehouseRepository).findById(warehouseTwo);
    }

    @Test
    @DisplayName("should throw WarehouseNotFoundException when a warehouse does not exist")
    void shouldThrowWarehouseNotFoundWhenWarehouseDoesNotExist() {
        UUID missingWarehouse = UUID.randomUUID();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, true,
                List.of(), List.of(), List.of(),
                List.of(missingWarehouse),
                false, null, null
        );

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.findById(missingWarehouse)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(request, GRANTED_BY));
        verify(warehouseAccessRepository, never()).save(any(CustomerWarehouseAccess.class));
    }

    @Test
    @DisplayName("should deactivate customer when active is false")
    void shouldDeactivateCustomerWhenActiveIsFalse() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test Corp", "BUSINESS", "J-12345678-9", "Test",
                30, BigDecimal.ZERO, false,
                List.of(), List.of(), List.of(),
                List.of(WAREHOUSE_ID),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());
        customer.deactivate();

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(WAREHOUSE_ID)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, false,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

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
                List.of(WAREHOUSE_ID),
                true, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(passwordEncoder.encode("J-12345678-9")).thenReturn("encoded-password");
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(WAREHOUSE_ID)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), true, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

        assertNotNull(response);
        assertTrue(response.portalAccess());
        verify(passwordEncoder).encode("J-12345678-9");
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
                List.of(WAREHOUSE_ID),
                false, "Some notes", null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(WAREHOUSE_ID)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, "Some notes", null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

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
                List.of(WAREHOUSE_ID),
                false, null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(2002L));
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(CustomerTestHelper.buildWarehouse(WAREHOUSE_ID)));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 2002L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), false, null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

        assertNotNull(response);
        assertEquals(2002L, response.code());
        verify(codeGenerator).generateNext();
    }
}
