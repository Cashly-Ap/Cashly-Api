package com.cashly.cashly_api.partners.customers.warehouse_access.application.grant.use_case;

import com.cashly.cashly_api.partners.customers.testing.CustomerTestHelper;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.model.WarehouseSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GrantWarehouseAccessUseCase Tests")
class GrantWarehouseAccessUseCaseTest {

    @Mock
    private CustomerWarehouseAccessRepository accessRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WarehouseDirectoryGateway warehouseRepository;

    private GrantWarehouseAccessUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID grantedBy = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GrantWarehouseAccessUseCase(accessRepository,
                customerRepository, warehouseRepository, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    }

    @Test
    @DisplayName("should grant warehouse access successfully")
    void shouldGrantWarehouseAccessSuccessfully() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        WarehouseSnapshot warehouse = new WarehouseSnapshot(warehouseId,
                "Main Warehouse");
        CustomerWarehouseAccess access = CustomerTestHelper
                .buildAccess(customerId, warehouseId);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(warehouseRepository.requireById(warehouseId))
                .thenReturn(warehouse);
        when(accessRepository.existsByCustomerIdAndWarehouseId(customerId,
                warehouseId)).thenReturn(false);
        when(accessRepository.save(any(CustomerWarehouseAccess.class)))
                .thenAnswer(inv -> {
                    CustomerWarehouseAccess saved = inv.getArgument(0);
                    return new CustomerWarehouseAccess(UUID.randomUUID(),
                            saved.getCustomerId(), saved.getWarehouseId(),
                            saved.getGrantedAt(), saved.getGrantedBy());
                });

        CustomerWarehouseAccessResponse response = useCase.execute(customerId,
                warehouseId, grantedBy);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
        assertEquals(warehouseId, response.warehouseId());
        assertEquals("Main Warehouse", response.warehouseName());
        verify(accessRepository).save(any(CustomerWarehouseAccess.class));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(warehouseRepository, never()).requireById(any());
        verify(accessRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(warehouseRepository.requireById(warehouseId))
                .thenThrow(new WarehouseNotFoundException(warehouseId));

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(warehouseRepository).requireById(warehouseId);
        verify(accessRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw CustomerWarehouseAccessAlreadyExistsException when access already exists")
    void shouldThrowWhenAccessAlreadyExists() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        WarehouseSnapshot warehouse = new WarehouseSnapshot(warehouseId,
                "Main Warehouse");

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(warehouseRepository.requireById(warehouseId))
                .thenReturn(warehouse);
        when(accessRepository.existsByCustomerIdAndWarehouseId(customerId,
                warehouseId)).thenReturn(true);

        assertThrows(CustomerWarehouseAccessAlreadyExistsException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(accessRepository, never()).save(any());
    }
}
