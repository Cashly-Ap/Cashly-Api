package com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.integration;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.model.WarehouseSnapshot;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryEntry;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalWarehouseDirectoryGatewayTest {
    private final WarehouseDirectoryQuery query = mock(
            WarehouseDirectoryQuery.class);
    private final LocalWarehouseDirectoryGateway gateway = new LocalWarehouseDirectoryGateway(
            query);

    @Test
    void should_map_public_warehouse_projection_to_customer_snapshot() {
        UUID id = UUID.randomUUID();
        when(query.findById(id)).thenReturn(
                Optional.of(new WarehouseDirectoryEntry(id, "Warehouse")));
        assertEquals(new WarehouseSnapshot(id, "Warehouse"),
                gateway.requireById(id));
    }

    @Test
    void should_preserve_not_found_type_and_message_for_required_warehouse() {
        UUID id = UUID.randomUUID();
        when(query.findById(id)).thenReturn(Optional.empty());
        assertEquals("Warehouse not found with id: " + id,
                assertThrows(WarehouseNotFoundException.class,
                        () -> gateway.requireById(id)).getMessage());
    }

    @Test
    void should_preserve_absent_warehouse_for_unknown_list_name() {
        UUID id = UUID.randomUUID();
        when(query.findById(id)).thenReturn(Optional.empty());
        assertTrue(gateway.findById(id).isEmpty());
    }
}
