package com.parsun.parsun_api.price_types.infrastructure.web;

import com.parsun.parsun_api.price_types.application.dto.request.CreatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.request.UpdatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<PriceTypeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getPriceTypeUseCase.execute(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<List<PriceTypeResponse>> listAll(
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir) {
        return ResponseEntity.ok(listPriceTypesUseCase.execute(orderBy, orderDir));
    }
}
