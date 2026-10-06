package com.cashly.cashly_api.warehouses.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.application.dto.request.CreateWarehouseRequest;
import com.cashly.cashly_api.warehouses.application.dto.request.UpdateWarehouseRequest;
import com.cashly.cashly_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.application.dto.response.WarehouseListResponse;
import com.cashly.cashly_api.warehouses.application.usecases.CreateWarehouseUseCase;
import com.cashly.cashly_api.warehouses.application.usecases.DeleteWarehouseUseCase;
import com.cashly.cashly_api.warehouses.application.usecases.GetWarehouseUseCase;
import com.cashly.cashly_api.warehouses.application.usecases.ListWarehousesUseCase;
import com.cashly.cashly_api.warehouses.application.usecases.UpdateWarehouseUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseController Unit Tests")
class WarehouseControllerTest {

    @Mock
    private CreateWarehouseUseCase createWarehouseUseCase;

    @Mock
    private GetWarehouseUseCase getWarehouseUseCase;

    @Mock
    private ListWarehousesUseCase listWarehousesUseCase;

    @Mock
    private UpdateWarehouseUseCase updateWarehouseUseCase;

    @Mock
    private DeleteWarehouseUseCase deleteWarehouseUseCase;

    @InjectMocks
    private WarehouseController warehouseController;

    @Test
    @DisplayName("should create warehouse and return created response")
    void should_create_warehouse_and_return_created_response() {
        String id = UUID.randomUUID().toString();
        WarehouseDetailResponse response = new WarehouseDetailResponse(
                id, "WH-01", "Main Warehouse", "Address", "State",
                "Municipality", "1000", true, true, LocalDateTime.now(), LocalDateTime.now());

        when(createWarehouseUseCase.execute(any(CreateWarehouseRequest.class))).thenReturn(response);

        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "WH-01", "Main Warehouse", "Address", "State", "Municipality", "1000");

        ResponseEntity<WarehouseDetailResponse> result = warehouseController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        assertEquals("Main Warehouse", result.getBody().name());
        verify(createWarehouseUseCase).execute(any(CreateWarehouseRequest.class));
    }

    @Test
    @DisplayName("should get warehouse by id")
    void should_get_warehouse_by_id() {
        String id = UUID.randomUUID().toString();
        WarehouseDetailResponse response = new WarehouseDetailResponse(
                id, "WH-01", "Main", "Addr", "State",
                "Municipality", "1000", true, true, LocalDateTime.now(), LocalDateTime.now());

        when(getWarehouseUseCase.execute(UUID.fromString(id))).thenReturn(response);

        ResponseEntity<WarehouseDetailResponse> result = warehouseController.getById(UUID.fromString(id));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        verify(getWarehouseUseCase).execute(UUID.fromString(id));
    }

    @Test
    @DisplayName("should list warehouses")
    void should_list_warehouses() {
        WarehouseListResponse wh = new WarehouseListResponse(
                UUID.randomUUID().toString(), "WH-01", "Main", "State",
                "Municipality", true, false, LocalDateTime.now());

        PageResponse<WarehouseListResponse> pageResponse = new PageResponse<>(
                List.of(wh), 1L, 1, 20, 0, true, true, false);

        when(listWarehousesUseCase.execute(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<WarehouseListResponse>> result = warehouseController.list(null, null, 0, 20, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().content().size());
        verify(listWarehousesUseCase).execute(null, null, 0, 20, "name", "asc");
    }

    @Test
    @DisplayName("should return empty list when no warehouses")
    void should_return_empty_list_when_no_warehouses() {
        PageResponse<WarehouseListResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(listWarehousesUseCase.execute(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        ResponseEntity<PageResponse<WarehouseListResponse>> result = warehouseController.list(null, null, 0, 20, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().content().isEmpty());
        verify(listWarehousesUseCase).execute(null, null, 0, 20, "name", "asc");
    }

    @Test
    @DisplayName("should update warehouse")
    void should_update_warehouse() {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        WarehouseDetailResponse response = new WarehouseDetailResponse(
                id, "WH-01", "Updated Warehouse", "Addr", "State",
                "Municipality", "1000", true, true, LocalDateTime.now(), LocalDateTime.now());

        when(updateWarehouseUseCase.execute(eq(uuid), any(UpdateWarehouseRequest.class))).thenReturn(response);

        UpdateWarehouseRequest request = new UpdateWarehouseRequest(
                "Updated Warehouse", "Addr", "State", "Municipality", "1000", true);

        ResponseEntity<WarehouseDetailResponse> result = warehouseController.update(uuid, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        assertEquals("Updated Warehouse", result.getBody().name());
        verify(updateWarehouseUseCase).execute(eq(uuid), any(UpdateWarehouseRequest.class));
    }

    @Test
    @DisplayName("should delete warehouse")
    void should_delete_warehouse() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteWarehouseUseCase).execute(id);

        ResponseEntity<Void> result = warehouseController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(deleteWarehouseUseCase).execute(id);
    }
}
