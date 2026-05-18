package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteWarehouseUseCase Tests")
class DeleteWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private DeleteWarehouseUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeleteWarehouseUseCase(warehouseRepository);
    }

    @Test
    @DisplayName("finds warehouse, deactivates, saves (soft delete)")
    void shouldDeactivateWarehouse() {
        Warehouse existing = Warehouse.reconstitute(
                warehouseId, "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(warehouseId);

        assertFalse(existing.isActive());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseRepository).save(existing);
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when not found")
    void shouldThrowWhenWarehouseNotFound() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId));

        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseRepository, never()).save(any());
    }
}
