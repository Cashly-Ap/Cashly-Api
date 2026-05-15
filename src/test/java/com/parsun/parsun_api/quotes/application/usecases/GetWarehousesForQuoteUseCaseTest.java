package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteWarehouseResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWarehousesForQuoteUseCase Unit Tests")
class GetWarehousesForQuoteUseCaseTest {

    @Mock
    private CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private GetWarehousesForQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetWarehousesForQuoteUseCase(customerWarehouseAccessRepository, warehouseRepository);
    }

    @Test
    @DisplayName("should return warehouses the customer has access to")
    void shouldReturnWarehousesCustomerHasAccessTo() {
        UUID customerId = UUID.randomUUID();
        UUID warehouse1Id = UUID.randomUUID();
        UUID warehouse2Id = UUID.randomUUID();

        List<CustomerWarehouseAccess> accesses = List.of(
                new CustomerWarehouseAccess(null, customerId, warehouse1Id, LocalDateTime.now(), customerId),
                new CustomerWarehouseAccess(null, customerId, warehouse2Id, LocalDateTime.now(), customerId)
        );

        Warehouse wh1 = Warehouse.reconstitute(warehouse1Id, "WH-001", "Main Warehouse",
                "123 Main St", null, null, null, true, LocalDateTime.now(), LocalDateTime.now());
        Warehouse wh2 = Warehouse.reconstitute(warehouse2Id, "WH-002", "Secondary Warehouse",
                "456 Second Ave", null, null, null, true, LocalDateTime.now(), LocalDateTime.now());

        when(customerWarehouseAccessRepository.findByCustomerId(customerId)).thenReturn(accesses);
        when(warehouseRepository.findById(warehouse1Id)).thenReturn(Optional.of(wh1));
        when(warehouseRepository.findById(warehouse2Id)).thenReturn(Optional.of(wh2));

        List<QuoteWarehouseResponse> result = useCase.execute(customerId);

        assertNotNull(result);
        assertEquals(2, result.size());

        QuoteWarehouseResponse resp1 = result.get(0);
        assertEquals(warehouse1Id.toString(), resp1.id());
        assertEquals("Main Warehouse", resp1.name());
        assertEquals("123 Main St", resp1.location());

        QuoteWarehouseResponse resp2 = result.get(1);
        assertEquals(warehouse2Id.toString(), resp2.id());
        assertEquals("Secondary Warehouse", resp2.name());
        assertEquals("456 Second Ave", resp2.location());

        verify(customerWarehouseAccessRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("should return empty list when customer has no warehouse access")
    void shouldReturnEmptyListWhenNoAccess() {
        UUID customerId = UUID.randomUUID();

        when(customerWarehouseAccessRepository.findByCustomerId(customerId)).thenReturn(List.of());

        List<QuoteWarehouseResponse> result = useCase.execute(customerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerWarehouseAccessRepository).findByCustomerId(customerId);
        verify(warehouseRepository, never()).findById(any());
    }

    @Test
    @DisplayName("should skip warehouses not found in repository")
    void shouldSkipWarehousesNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID warehouse1Id = UUID.randomUUID();
        UUID warehouse2Id = UUID.randomUUID();

        List<CustomerWarehouseAccess> accesses = List.of(
                new CustomerWarehouseAccess(null, customerId, warehouse1Id, LocalDateTime.now(), customerId),
                new CustomerWarehouseAccess(null, customerId, warehouse2Id, LocalDateTime.now(), customerId)
        );

        Warehouse wh1 = Warehouse.reconstitute(warehouse1Id, "WH-001", "Main Warehouse",
                "123 Main St", null, null, null, true, LocalDateTime.now(), LocalDateTime.now());

        when(customerWarehouseAccessRepository.findByCustomerId(customerId)).thenReturn(accesses);
        when(warehouseRepository.findById(warehouse1Id)).thenReturn(Optional.of(wh1));
        when(warehouseRepository.findById(warehouse2Id)).thenReturn(Optional.empty());

        List<QuoteWarehouseResponse> result = useCase.execute(customerId);

        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.get(0).name());
    }
}
