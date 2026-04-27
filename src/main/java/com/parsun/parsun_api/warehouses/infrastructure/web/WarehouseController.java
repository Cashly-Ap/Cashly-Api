package com.parsun.parsun_api.warehouses.infrastructure.web;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.application.dto.request.CreateWarehouseRequest;
import com.parsun.parsun_api.warehouses.application.dto.request.UpdateWarehouseRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseListResponse;
import com.parsun.parsun_api.warehouses.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public WarehouseDetailResponse create(@Valid @RequestBody CreateWarehouseRequest request) {
        return createWarehouseUseCase.execute(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public WarehouseDetailResponse getById(@PathVariable UUID id) {
        return getWarehouseUseCase.execute(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public PageResponse<WarehouseListResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return listWarehousesUseCase.execute(search, active, page, size, orderBy, orderDir);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public WarehouseDetailResponse update(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateWarehouseRequest request) {
        return updateWarehouseUseCase.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public void delete(@PathVariable UUID id) {
        deleteWarehouseUseCase.execute(id);
    }
}
