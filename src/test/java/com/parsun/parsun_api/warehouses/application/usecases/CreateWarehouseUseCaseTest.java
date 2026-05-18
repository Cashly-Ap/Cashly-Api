package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.warehouses.application.dto.request.CreateWarehouseRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.DuplicateWarehouseCodeException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateWarehouseUseCase Tests")
class CreateWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWarehouseUseCase(warehouseRepository);
    }

    @Test
    @DisplayName("creates warehouse, saves, returns detail")
    void shouldCreateWarehouseAndReturnDetail() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001"
        );

        when(warehouseRepository.existsByCode("WH-001")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> {
            Warehouse w = inv.getArgument(0);
            return Warehouse.reconstitute(
                    UUID.randomUUID(), w.getCode(), w.getName(), w.getAddress(),
                    w.getState(), w.getMunicipality(), w.getPostalCode(),
                    w.isActive(), w.getCreatedAt(), w.getUpdatedAt()
            );
        });

        WarehouseDetailResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("WH-001", response.code());
        assertEquals("Main Warehouse", response.name());
        assertEquals("123 Main St", response.address());
        assertTrue(response.active());
        verify(warehouseRepository).existsByCode("WH-001");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("throws DuplicateWarehouseCodeException when code exists")
    void shouldThrowWhenCodeAlreadyExists() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "WH-001", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001"
        );

        when(warehouseRepository.existsByCode("WH-001")).thenReturn(true);

        assertThrows(DuplicateWarehouseCodeException.class, () -> useCase.execute(request));

        verify(warehouseRepository).existsByCode("WH-001");
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    @DisplayName("code is trimmed and uppercased")
    void shouldTrimAndUppercaseCode() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "  wh-001  ", "Main Warehouse", "123 Main St",
                "State A", "Municipality B", "10001"
        );

        when(warehouseRepository.existsByCode("WH-001")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> {
            Warehouse w = inv.getArgument(0);
            return Warehouse.reconstitute(
                    UUID.randomUUID(), w.getCode(), w.getName(), w.getAddress(),
                    w.getState(), w.getMunicipality(), w.getPostalCode(),
                    w.isActive(), w.getCreatedAt(), w.getUpdatedAt()
            );
        });

        WarehouseDetailResponse response = useCase.execute(request);

        assertEquals("WH-001", response.code());
        verify(warehouseRepository).existsByCode("WH-001");
    }
}
