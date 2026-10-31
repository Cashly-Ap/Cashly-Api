package com.cashly.cashly_api.partners.customers.warehouse_access.application.revoke.use_case;

import com.cashly.cashly_api.partners.customers.testing.CustomerTestHelper;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.exceptions.CustomerWarehouseAccessNotFoundException;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeWarehouseAccessUseCase Tests")
class RevokeWarehouseAccessUseCaseTest {

    @Mock
    private CustomerWarehouseAccessRepository accessRepository;

    private RevokeWarehouseAccessUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RevokeWarehouseAccessUseCase(accessRepository);
    }

    @Test
    @DisplayName("should revoke warehouse access successfully")
    void shouldRevokeWarehouseAccessSuccessfully() {
        CustomerWarehouseAccess access = CustomerTestHelper
                .buildAccess(customerId, warehouseId);

        when(accessRepository.findByCustomerIdAndWarehouseId(customerId,
                warehouseId)).thenReturn(Optional.of(access));

        useCase.execute(customerId, warehouseId);

        verify(accessRepository).findByCustomerIdAndWarehouseId(customerId,
                warehouseId);
        verify(accessRepository).delete(access);
    }

    @Test
    @DisplayName("should throw CustomerWarehouseAccessNotFoundException when access not found")
    void shouldThrowWhenAccessNotFound() {
        when(accessRepository.findByCustomerIdAndWarehouseId(customerId,
                warehouseId)).thenReturn(Optional.empty());

        assertThrows(CustomerWarehouseAccessNotFoundException.class,
                () -> useCase.execute(customerId, warehouseId));

        verify(accessRepository).findByCustomerIdAndWarehouseId(customerId,
                warehouseId);
        verify(accessRepository, never()).delete(any());
    }
}
