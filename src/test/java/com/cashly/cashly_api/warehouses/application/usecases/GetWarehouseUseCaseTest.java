package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWarehouseUseCase Tests")
class GetWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private GetWarehouseUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetWarehouseUseCase(warehouseRepository);
    }

    @Test
    @DisplayName("returns detail response when found")
    void shouldReturnWarehouseDetail() {
        Warehouse existing = Warehouse.reconstitute(
                warehouseId, "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existing));
        when(warehouseRepository.findIdsWithActiveStock(Set.of(warehouseId))).thenReturn(Set.of());

        WarehouseDetailResponse response = useCase.execute(warehouseId);

        assertNotNull(response);
        assertEquals(warehouseId.toString(), response.id());
        assertEquals("WH-001", response.code());
        assertEquals("Main Warehouse", response.name());
        assertEquals("123 Main St", response.address());
        assertEquals("State A", response.state());
        assertEquals("Municipality B", response.municipality());
        assertEquals("10001", response.postalCode());
        assertTrue(response.active());
        assertTrue(response.deletable());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseRepository).findIdsWithActiveStock(Set.of(warehouseId));
    }

    @Test
    @DisplayName("marks warehouse as non-deletable when it has active stock")
    void shouldMarkWarehouseAsNonDeletableWhenItHasActiveStock() {
        Warehouse existing = Warehouse.reconstitute(
                warehouseId, "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existing));
        when(warehouseRepository.findIdsWithActiveStock(Set.of(warehouseId)))
                .thenReturn(Set.of(warehouseId));

        WarehouseDetailResponse response = useCase.execute(warehouseId);

        assertFalse(response.deletable());
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when not found")
    void shouldThrowWhenWarehouseNotFound() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId));

        verify(warehouseRepository).findById(warehouseId);
    }
}
