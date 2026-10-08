package com.cashly.cashly_api.warehouses.application.services;

import com.cashly.cashly_api.warehouses.application.api.WarehouseDirectoryEntry;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryWarehouseDirectoryQueryTest {
    @Test
    void should_return_only_directory_data_from_owned_warehouse_repository() {
        UUID id = UUID.randomUUID();
        Warehouse warehouse = mock(Warehouse.class);
        WarehouseRepository repository = mock(WarehouseRepository.class);
        when(warehouse.getId()).thenReturn(id);
        when(warehouse.getName()).thenReturn("Warehouse");
        when(warehouse.getAddress()).thenReturn("Address");
        when(repository.findById(id)).thenReturn(Optional.of(warehouse));
        assertEquals(Optional.of(new WarehouseDirectoryEntry(id, "Warehouse", "Address")),
                new RepositoryWarehouseDirectoryQuery(repository).findById(id));
        verify(repository).findById(id);
    }

    @Test
    void should_return_absent_when_warehouse_does_not_exist() {
        UUID id = UUID.randomUUID();
        WarehouseRepository repository = mock(WarehouseRepository.class);
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertTrue(new RepositoryWarehouseDirectoryQuery(repository).findById(id).isEmpty());
    }
}
