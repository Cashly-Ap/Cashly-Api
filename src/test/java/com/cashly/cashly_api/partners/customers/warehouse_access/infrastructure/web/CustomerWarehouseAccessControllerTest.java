package com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.web;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.grant.use_case.GrantWarehouseAccessUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.list.use_case.ListCustomerWarehousesUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.revoke.use_case.RevokeWarehouseAccessUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerWarehouseAccessController Unit Tests")
class CustomerWarehouseAccessControllerTest {

    @Mock
    private GrantWarehouseAccessUseCase grantWarehouseAccessUseCase;

    @Mock
    private RevokeWarehouseAccessUseCase revokeWarehouseAccessUseCase;

    @Mock
    private ListCustomerWarehousesUseCase listCustomerWarehousesUseCase;

    @InjectMocks
    private CustomerWarehouseAccessController customerWarehouseAccessController;

    @Test
    @DisplayName("should list warehouses and return 200")
    void should_list_warehouses_and_return_200() {
        UUID customerId = UUID.randomUUID();
        List<CustomerWarehouseAccessResponse> responses = List
                .of(new CustomerWarehouseAccessResponse(UUID.randomUUID(),
                        customerId, UUID.randomUUID(), "Main Warehouse"));

        when(listCustomerWarehousesUseCase.execute(customerId))
                .thenReturn(responses);

        ResponseEntity<List<CustomerWarehouseAccessResponse>> result = customerWarehouseAccessController
                .listWarehouses(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(listCustomerWarehousesUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should list empty warehouses when customer has no access")
    void should_list_empty_warehouses_when_customer_has_no_access() {
        UUID customerId = UUID.randomUUID();

        when(listCustomerWarehousesUseCase.execute(customerId))
                .thenReturn(List.of());

        ResponseEntity<List<CustomerWarehouseAccessResponse>> result = customerWarehouseAccessController
                .listWarehouses(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(listCustomerWarehousesUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should grant warehouse access and return 201")
    void should_grant_warehouse_access_and_return_201() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID grantedBy = UUID.randomUUID();

        CustomerWarehouseAccessResponse response = new CustomerWarehouseAccessResponse(
                UUID.randomUUID(), customerId, warehouseId, "Main Warehouse");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(grantedBy.toString());

        when(grantWarehouseAccessUseCase.execute(customerId, warehouseId,
                grantedBy)).thenReturn(response);

        ResponseEntity<CustomerWarehouseAccessResponse> result = customerWarehouseAccessController
                .grantAccess(customerId, warehouseId, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(customerId, result.getBody().customerId());
        assertEquals(warehouseId, result.getBody().warehouseId());
        verify(grantWarehouseAccessUseCase).execute(customerId, warehouseId,
                grantedBy);
    }

    @Test
    @DisplayName("should revoke warehouse access and return 204")
    void should_revoke_warehouse_access_and_return_204() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        doNothing().when(revokeWarehouseAccessUseCase).execute(customerId,
                warehouseId);

        ResponseEntity<Void> result = customerWarehouseAccessController
                .revokeAccess(customerId, warehouseId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(revokeWarehouseAccessUseCase).execute(customerId, warehouseId);
    }
}
