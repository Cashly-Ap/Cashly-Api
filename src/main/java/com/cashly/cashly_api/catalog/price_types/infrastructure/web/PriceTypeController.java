package com.cashly.cashly_api.catalog.price_types.infrastructure.web;

import com.cashly.cashly_api.catalog.price_types.application.create.request.CreatePriceTypeRequest;
import com.cashly.cashly_api.catalog.price_types.application.update.request.UpdatePriceTypeRequest;
import com.cashly.cashly_api.catalog.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.catalog.price_types.application.create.use_case.CreatePriceTypeUseCase;
import com.cashly.cashly_api.catalog.price_types.application.delete.use_case.DeletePriceTypeUseCase;
import com.cashly.cashly_api.catalog.price_types.application.queries.use_case.GetPriceTypeUseCase;
import com.cashly.cashly_api.catalog.price_types.application.queries.use_case.ListPriceTypesUseCase;
import com.cashly.cashly_api.catalog.price_types.application.update.use_case.UpdatePriceTypeUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/price-types")
public class PriceTypeController {

    private final CreatePriceTypeUseCase createPriceTypeUseCase;
    private final UpdatePriceTypeUseCase updatePriceTypeUseCase;
    private final DeletePriceTypeUseCase deletePriceTypeUseCase;
    private final GetPriceTypeUseCase getPriceTypeUseCase;
    private final ListPriceTypesUseCase listPriceTypesUseCase;

    public PriceTypeController(CreatePriceTypeUseCase createPriceTypeUseCase,
                                UpdatePriceTypeUseCase updatePriceTypeUseCase,
                                DeletePriceTypeUseCase deletePriceTypeUseCase,
                                GetPriceTypeUseCase getPriceTypeUseCase,
                                ListPriceTypesUseCase listPriceTypesUseCase) {
        this.createPriceTypeUseCase = createPriceTypeUseCase;
        this.updatePriceTypeUseCase = updatePriceTypeUseCase;
        this.deletePriceTypeUseCase = deletePriceTypeUseCase;
        this.getPriceTypeUseCase = getPriceTypeUseCase;
        this.listPriceTypesUseCase = listPriceTypesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<PriceTypeResponse> create(@Valid @RequestBody CreatePriceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createPriceTypeUseCase.execute(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<PriceTypeResponse> update(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdatePriceTypeRequest request) {
        return ResponseEntity.ok(updatePriceTypeUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePriceTypeUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY') or hasAuthority('MODULE_SALES')")
    public ResponseEntity<PriceTypeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getPriceTypeUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY') or hasAuthority('MODULE_SALES')")
    public ResponseEntity<PageResponse<PriceTypeResponse>> listAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return ResponseEntity.ok(listPriceTypesUseCase.execute(search, active, page, size, orderBy, orderDir));
    }
}
