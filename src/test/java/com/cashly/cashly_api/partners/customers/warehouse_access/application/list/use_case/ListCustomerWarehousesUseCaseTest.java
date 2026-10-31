package com.cashly.cashly_api.partners.customers.warehouse_access.application.list.use_case;

import com.cashly.cashly_api.partners.customers.testing.CustomerTestHelper;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.model.WarehouseSnapshot;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCustomerWarehousesUseCase Tests")
class ListCustomerWarehousesUseCaseTest {

    @Mock
    private CustomerWarehouseAccessRepository accessRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WarehouseDirectoryGateway warehouseRepository;

    private ListCustomerWarehousesUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ListCustomerWarehousesUseCase(accessRepository,
                customerRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should return list of warehouse access responses with warehouse names")
    void shouldReturnListOfWarehouseAccessResponses() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        UUID warehouseId1 = UUID.randomUUID();
        UUID warehouseId2 = UUID.randomUUID();

        CustomerWarehouseAccess access1 = CustomerTestHelper
                .buildAccess(customerId, warehouseId1);
        CustomerWarehouseAccess access2 = CustomerTestHelper
                .buildAccess(customerId, warehouseId2);

        WarehouseSnapshot warehouse1 = new WarehouseSnapshot(warehouseId1,
                "Main Warehouse");
        WarehouseSnapshot warehouse2 = new WarehouseSnapshot(warehouseId2,
                "Main Warehouse");

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(accessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access1, access2));
        when(warehouseRepository.findById(warehouseId1))
                .thenReturn(Optional.of(warehouse1));
        when(warehouseRepository.findById(warehouseId2))
                .thenReturn(Optional.of(warehouse2));

        List<CustomerWarehouseAccessResponse> response = useCase
                .execute(customerId);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Main Warehouse", response.get(0).warehouseName());
        assertEquals("Main Warehouse", response.get(1).warehouseName());
        verify(customerRepository).findById(new CustomerId(customerId));
        verify(accessRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(accessRepository, never()).findByCustomerId(any());
    }

    @Test
    @DisplayName("should use Unknown as name when warehouse not found for access")
    void shouldUseUnknownWhenWarehouseNotFound() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        UUID warehouseId = UUID.randomUUID();

        CustomerWarehouseAccess access = CustomerTestHelper
                .buildAccess(customerId, warehouseId);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(accessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.empty());

        List<CustomerWarehouseAccessResponse> response = useCase
                .execute(customerId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Unknown", response.get(0).warehouseName());
    }
}
