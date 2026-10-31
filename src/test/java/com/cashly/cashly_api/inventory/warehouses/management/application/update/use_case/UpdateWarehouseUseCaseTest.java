package com.cashly.cashly_api.inventory.warehouses.management.application.update.use_case;

import com.cashly.cashly_api.inventory.warehouses.management.application.update.request.UpdateWarehouseRequest;
import com.cashly.cashly_api.inventory.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
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
@DisplayName("UpdateWarehouseUseCase Tests")
class UpdateWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private UpdateWarehouseUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateWarehouseUseCase(warehouseRepository);
    }

    @Test
    @DisplayName("finds warehouse, updates, saves")
    void shouldUpdateWarehouseAndReturnDetail() {
        Warehouse existing = Warehouse.reconstitute(warehouseId, "WH-001",
                "Old Name", "Old Addr", "Old State", "Old Mun", "10001", true,
                LocalDateTime.now(), LocalDateTime.now());

        UpdateWarehouseRequest request = new UpdateWarehouseRequest("New Name",
                "New Addr", "New State", "New Mun", "20002", null);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WarehouseDetailResponse response = useCase.execute(warehouseId,
                request);

        assertNotNull(response);
        assertEquals("New Name", response.name());
        assertEquals("New Addr", response.address());
        assertEquals("New State", response.state());
        assertEquals("New Mun", response.municipality());
        assertEquals("20002", response.postalCode());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when not found")
    void shouldThrowWhenWarehouseNotFound() {
        UpdateWarehouseRequest request = new UpdateWarehouseRequest("New Name",
                "New Addr", "New State", "New Mun", "20002", null);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(warehouseId, request));

        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseRepository, never()).save(any());
    }
}
