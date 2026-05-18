package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseListResponse;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListWarehousesUseCase Tests")
class ListWarehousesUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private ListWarehousesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListWarehousesUseCase(warehouseRepository);
    }

    @Test
    @DisplayName("returns paginated list")
    void shouldReturnPaginatedList() {
        Warehouse w1 = Warehouse.reconstitute(
                UUID.randomUUID(), "WH-001", "Warehouse 1", "Addr 1",
                "State A", "Mun A", "10001", true, LocalDateTime.now(), LocalDateTime.now()
        );
        Warehouse w2 = Warehouse.reconstitute(
                UUID.randomUUID(), "WH-002", "Warehouse 2", "Addr 2",
                "State B", "Mun B", "10002", true, LocalDateTime.now(), LocalDateTime.now()
        );

        PageResponse<Warehouse> page = new PageResponse<>(
                List.of(w1, w2), 2L, 1, 10, 0, true, true, false
        );

        when(warehouseRepository.findAllWithFilters(null, null, 0, 10, "name", "asc"))
                .thenReturn(page);

        PageResponse<WarehouseListResponse> response = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(response);
        assertEquals(2, response.content().size());
        assertEquals(2L, response.totalElements());
        assertEquals(1, response.totalPages());
        assertFalse(response.empty());
        verify(warehouseRepository).findAllWithFilters(null, null, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("passes search and active filters to repository")
    void shouldPassFiltersToRepository() {
        PageResponse<Warehouse> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(warehouseRepository.findAllWithFilters("main", true, 0, 20, "code", "desc"))
                .thenReturn(page);

        PageResponse<WarehouseListResponse> response = useCase.execute("main", true, 0, 20, "code", "desc");

        assertNotNull(response);
        assertTrue(response.empty());
        verify(warehouseRepository).findAllWithFilters("main", true, 0, 20, "code", "desc");
    }

    @Test
    @DisplayName("returns empty page when no results")
    void shouldReturnEmptyPage() {
        PageResponse<Warehouse> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(warehouseRepository.findAllWithFilters("nonexistent", null, 0, 10, "name", "asc"))
                .thenReturn(page);

        PageResponse<WarehouseListResponse> response = useCase.execute("nonexistent", null, 0, 10, "name", "asc");

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0L, response.totalElements());
        assertTrue(response.empty());
    }
}
