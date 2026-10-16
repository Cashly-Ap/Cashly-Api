package com.cashly.cashly_api.warehouses.management.infrastructure.web;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.management.application.create.request.CreateWarehouseRequest;
import com.cashly.cashly_api.warehouses.management.application.update.request.UpdateWarehouseRequest;
import com.cashly.cashly_api.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.management.application.dto.response.WarehouseListResponse;
import com.cashly.cashly_api.warehouses.management.application.create.use_case.CreateWarehouseUseCase;
import com.cashly.cashly_api.warehouses.management.application.delete.use_case.DeleteWarehouseUseCase;
import com.cashly.cashly_api.warehouses.management.application.queries.use_case.GetWarehouseUseCase;
import com.cashly.cashly_api.warehouses.management.application.queries.use_case.ListWarehousesUseCase;
import com.cashly.cashly_api.warehouses.management.application.update.use_case.UpdateWarehouseUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final GetWarehouseUseCase getWarehouseUseCase;
    private final ListWarehousesUseCase listWarehousesUseCase;
    private final UpdateWarehouseUseCase updateWarehouseUseCase;
    private final DeleteWarehouseUseCase deleteWarehouseUseCase;

    public WarehouseController(CreateWarehouseUseCase createWarehouseUseCase,
                                GetWarehouseUseCase getWarehouseUseCase,
                                ListWarehousesUseCase listWarehousesUseCase,
                                UpdateWarehouseUseCase updateWarehouseUseCase,
                                DeleteWarehouseUseCase deleteWarehouseUseCase) {
        this.createWarehouseUseCase = createWarehouseUseCase;
        this.getWarehouseUseCase = getWarehouseUseCase;
        this.listWarehousesUseCase = listWarehousesUseCase;
        this.updateWarehouseUseCase = updateWarehouseUseCase;
        this.deleteWarehouseUseCase = deleteWarehouseUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<WarehouseDetailResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createWarehouseUseCase.execute(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<WarehouseDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getWarehouseUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<PageResponse<WarehouseListResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return ResponseEntity.ok(listWarehousesUseCase.execute(search, active, page, size, orderBy, orderDir));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<WarehouseDetailResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(updateWarehouseUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteWarehouseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
