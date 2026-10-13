package com.cashly.cashly_api.suppliers.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.suppliers.application.dto.request.CreateSupplierRequest;
import com.cashly.cashly_api.suppliers.application.dto.request.UpdateSupplierRequest;
import com.cashly.cashly_api.suppliers.application.dto.response.SupplierCreateResponse;
import com.cashly.cashly_api.suppliers.application.dto.response.SupplierDetailResponse;
import com.cashly.cashly_api.suppliers.application.dto.response.SupplierListResponse;
import com.cashly.cashly_api.suppliers.application.usecases.*;
import com.cashly.cashly_api.shared.dto.PageResponse;
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
@DisplayName("SupplierController Unit Tests")
class SupplierControllerTest {

    @Mock
    private RegisterSupplierUseCase registerSupplierUseCase;

    @Mock
    private GetSupplierUseCase getSupplierUseCase;

    @Mock
    private ListSuppliersUseCase listSuppliersUseCase;

    @Mock
    private UpdateSupplierUseCase updateSupplierUseCase;

    @Mock
    private DeleteSupplierUseCase deleteSupplierUseCase;

    @InjectMocks
    private SupplierController supplierController;

    @Test
    @DisplayName("should create supplier and return 201")
    void should_create_supplier_and_return_201() {
        String supplierId = UUID.randomUUID().toString();
        SupplierCreateResponse response = new SupplierCreateResponse(
                supplierId, 1001L, "Proveedor C.A.", "Proveedor", "JURIDICO", "J-12345678-9");

        when(registerSupplierUseCase.execute(any(CreateSupplierRequest.class))).thenReturn(response);

        CreateSupplierRequest request = new CreateSupplierRequest(
                "Proveedor C.A.", "JURIDICO", "J-12345678-9", "Proveedor",
                null, List.of(), List.of(), List.of());

        ResponseEntity<SupplierCreateResponse> result = supplierController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(supplierId, result.getBody().id());
        verify(registerSupplierUseCase).execute(any(CreateSupplierRequest.class));
    }

    @Test
    @DisplayName("should get all suppliers and return 200")
    void should_get_all_suppliers_and_return_200() {
        SupplierListResponse supplier = new SupplierListResponse(
                UUID.randomUUID().toString(), 1001L, "Proveedor C.A.", "Proveedor",
                "JURIDICO", "J-12345678-9", true, LocalDateTime.now().toString(), false);

        PageResponse<SupplierListResponse> pageResponse = new PageResponse<>(
                List.of(supplier), 1L, 1, 10, 0, true, true, false);

        when(listSuppliersUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<SupplierListResponse>> result = supplierController.getAll(
                null, null, null, null, 0, 10, "legalName", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        verify(listSuppliersUseCase).execute(null, null, null, null, 0, 10, "legalName", "asc");
    }

    @Test
    @DisplayName("should get empty suppliers list")
    void should_get_empty_suppliers_list() {
        PageResponse<SupplierListResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(listSuppliersUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        ResponseEntity<PageResponse<SupplierListResponse>> result = supplierController.getAll(
                null, null, null, null, 0, 10, "legalName", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
        verify(listSuppliersUseCase).execute(null, null, null, null, 0, 10, "legalName", "asc");
    }

    @Test
    @DisplayName("should get supplier by id and return 200")
    void should_get_supplier_by_id_and_return_200() {
        String id = UUID.randomUUID().toString();
        SupplierDetailResponse response = new SupplierDetailResponse(
                id, 1001L, "Proveedor C.A.", "Proveedor", "JURIDICO", "J-12345678-9",
                true, null, LocalDateTime.now(), List.of(), List.of(), List.of());

        when(getSupplierUseCase.execute(id)).thenReturn(response);

        ResponseEntity<SupplierDetailResponse> result = supplierController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getSupplierUseCase).execute(id);
    }

    @Test
    @DisplayName("should update supplier and return 200")
    void should_update_supplier_and_return_200() {
        String id = UUID.randomUUID().toString();
        SupplierDetailResponse response = new SupplierDetailResponse(
                id, 1001L, "Updated C.A.", "Updated", "NATURAL", "V-87654321-1",
                true, null, LocalDateTime.now(), List.of(), List.of(), List.of());

        when(updateSupplierUseCase.execute(eq(id), any(UpdateSupplierRequest.class))).thenReturn(response);

        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "Updated C.A.", "NATURAL", "V-87654321-1", "Updated",
                true, null, List.of(), List.of(), List.of());

        ResponseEntity<SupplierDetailResponse> result = supplierController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(updateSupplierUseCase).execute(eq(id), any(UpdateSupplierRequest.class));
    }

    @Test
    @DisplayName("should delete supplier and return 204")
    void should_delete_supplier_and_return_204() {
        String id = UUID.randomUUID().toString();
        doNothing().when(deleteSupplierUseCase).execute(id);

        ResponseEntity<Void> result = supplierController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteSupplierUseCase).execute(id);
    }
}
