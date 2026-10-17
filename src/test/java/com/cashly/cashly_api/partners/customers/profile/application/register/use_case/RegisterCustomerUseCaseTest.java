package com.cashly.cashly_api.partners.customers.profile.application.register.use_case;

import com.cashly.cashly_api.partners.customers.testing.CustomerTestHelper;

import com.cashly.cashly_api.partners.customers.profile.application.dto.request.AddressRequest;
import com.cashly.cashly_api.partners.customers.profile.application.register.request.CreateCustomerRequest;
import com.cashly.cashly_api.partners.customers.profile.application.dto.request.EmailRequest;
import com.cashly.cashly_api.partners.customers.profile.application.dto.request.PhoneRequest;
import com.cashly.cashly_api.partners.customers.profile.application.register.response.CustomerCreateResponse;
import com.cashly.cashly_api.partners.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.services.CustomerCodeGenerator;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerCode;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.Rif;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.model.WarehouseSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private CustomerWarehouseAccessRepository warehouseAccessRepository;

    @Mock
    private WarehouseDirectoryGateway warehouseRepository;

    private RegisterCustomerUseCase useCase;

    private static final UUID GRANTED_BY = UUID.randomUUID();
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegisterCustomerUseCase(customerRepository, codeGenerator, mapper,
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
                null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.requireById(WAREHOUSE_ID)).thenReturn(new WarehouseSnapshot(WAREHOUSE_ID, "Main Warehouse"));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), null, null
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
                null, null
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
                null, null
        );

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.requireById(warehouseOne)).thenReturn(new WarehouseSnapshot(warehouseOne, "Main Warehouse"));
        when(warehouseRepository.requireById(warehouseTwo)).thenReturn(new WarehouseSnapshot(warehouseTwo, "Main Warehouse"));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(new CustomerCreateResponse(
                UUID.randomUUID().toString(), 1001L, "Test Corp", "BUSINESS",
                "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                null, null, List.of(), List.of(), List.of(), null, null
        ));

        useCase.execute(request, GRANTED_BY);

        verify(warehouseAccessRepository, times(2)).save(any(CustomerWarehouseAccess.class));
        verify(warehouseRepository).requireById(warehouseOne);
        verify(warehouseRepository).requireById(warehouseTwo);
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
                null, null
        );

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.requireById(missingWarehouse)).thenThrow(new WarehouseNotFoundException(missingWarehouse));
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
                null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());
        customer.deactivate();

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.requireById(WAREHOUSE_ID)).thenReturn(new WarehouseSnapshot(WAREHOUSE_ID, "Main Warehouse"));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, false,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

        assertNotNull(response);
        assertFalse(response.active());
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
                "Some notes", null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(1001L));
        when(warehouseRepository.requireById(WAREHOUSE_ID)).thenReturn(new WarehouseSnapshot(WAREHOUSE_ID, "Main Warehouse"));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 1001L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), "Some notes", null
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
                null, null
        );

        Customer customer = CustomerTestHelper.buildCustomer(UUID.randomUUID());

        when(codeGenerator.generateNext()).thenReturn(new CustomerCode(2002L));
        when(warehouseRepository.requireById(WAREHOUSE_ID)).thenReturn(new WarehouseSnapshot(WAREHOUSE_ID, "Main Warehouse"));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toCreateResponse(any(Customer.class))).thenReturn(
                new CustomerCreateResponse(
                        customer.getId().toString(), 2002L, "Test Corp", "BUSINESS",
                        "J-12345678-9", "Test", 30, BigDecimal.ZERO, true,
                        customer.getPaymentType(), customer.getCreatedAt(),
                        List.of(), List.of(), List.of(), null, null
                )
        );

        CustomerCreateResponse response = useCase.execute(request, GRANTED_BY);

        assertNotNull(response);
        assertEquals(2002L, response.code());
        verify(codeGenerator).generateNext();
    }
}
