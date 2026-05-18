package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerWarehouseAccessResponse;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
    private WarehouseRepository warehouseRepository;

    private GrantWarehouseAccessUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID grantedBy = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GrantWarehouseAccessUseCase(accessRepository, customerRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should grant warehouse access successfully")
    void shouldGrantWarehouseAccessSuccessfully() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        Warehouse warehouse = CustomerTestHelper.buildWarehouse(warehouseId);
        CustomerWarehouseAccess access = CustomerTestHelper.buildAccess(customerId, warehouseId);

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(accessRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId)).thenReturn(false);
        when(accessRepository.save(any(CustomerWarehouseAccess.class))).thenAnswer(inv -> {
            CustomerWarehouseAccess saved = inv.getArgument(0);
            return new CustomerWarehouseAccess(
                    UUID.randomUUID(), saved.getCustomerId(), saved.getWarehouseId(),
                    saved.getGrantedAt(), saved.getGrantedBy()
            );
        });

        CustomerWarehouseAccessResponse response = useCase.execute(customerId, warehouseId, grantedBy);

        assertNotNull(response);
        assertEquals(customerId, response.customerId());
        assertEquals(warehouseId, response.warehouseId());
        assertEquals("Main Warehouse", response.warehouseName());
        verify(accessRepository).save(any(CustomerWarehouseAccess.class));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(warehouseRepository, never()).findById(any());
        verify(accessRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(warehouseRepository).findById(warehouseId);
        verify(accessRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw CustomerWarehouseAccessAlreadyExistsException when access already exists")
    void shouldThrowWhenAccessAlreadyExists() {
        Customer customer = CustomerTestHelper.buildCustomer(customerId);
        Warehouse warehouse = CustomerTestHelper.buildWarehouse(warehouseId);

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(accessRepository.existsByCustomerIdAndWarehouseId(customerId, warehouseId)).thenReturn(true);

        assertThrows(CustomerWarehouseAccessAlreadyExistsException.class,
                () -> useCase.execute(customerId, warehouseId, grantedBy));

        verify(accessRepository, never()).save(any());
    }
}
