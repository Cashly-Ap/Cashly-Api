package com.cashly.cashly_api.partners.customers.warehouse_access.infrastructure.web;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.dto.response.CustomerWarehouseAccessResponse;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.grant.use_case.GrantWarehouseAccessUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.list.use_case.ListCustomerWarehousesUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.revoke.use_case.RevokeWarehouseAccessUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/warehouses")
public class CustomerWarehouseAccessController {

    private final GrantWarehouseAccessUseCase grantWarehouseAccessUseCase;
    private final RevokeWarehouseAccessUseCase revokeWarehouseAccessUseCase;
    private final ListCustomerWarehousesUseCase listCustomerWarehousesUseCase;

    public CustomerWarehouseAccessController(
            GrantWarehouseAccessUseCase grantWarehouseAccessUseCase,
            RevokeWarehouseAccessUseCase revokeWarehouseAccessUseCase,
            ListCustomerWarehousesUseCase listCustomerWarehousesUseCase) {
        this.grantWarehouseAccessUseCase = grantWarehouseAccessUseCase;
        this.revokeWarehouseAccessUseCase = revokeWarehouseAccessUseCase;
        this.listCustomerWarehousesUseCase = listCustomerWarehousesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES_VIEW_LIST')")
    public ResponseEntity<List<CustomerWarehouseAccessResponse>> listWarehouses(
            @PathVariable UUID customerId) {
        return ResponseEntity
                .ok(listCustomerWarehousesUseCase.execute(customerId));
    }

    @PostMapping("/{warehouseId}")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES_EDIT')")
    public ResponseEntity<CustomerWarehouseAccessResponse> grantAccess(
            @PathVariable UUID customerId, @PathVariable UUID warehouseId,
            Authentication authentication) {
        UUID grantedBy = UUID.fromString(authentication.getName());
        CustomerWarehouseAccessResponse response = grantWarehouseAccessUseCase
                .execute(customerId, warehouseId, grantedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES_EDIT')")
    public ResponseEntity<Void> revokeAccess(@PathVariable UUID customerId,
            @PathVariable UUID warehouseId) {
        revokeWarehouseAccessUseCase.execute(customerId, warehouseId);
        return ResponseEntity.noContent().build();
    }
}
