package com.cashly.cashly_api.partners.customers.warehouse_access.application.dto.response;

import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;

import java.util.UUID;

public record CustomerWarehouseAccessResponse(UUID id, UUID customerId,
        UUID warehouseId, String warehouseName) {
    public static CustomerWarehouseAccessResponse from(
            CustomerWarehouseAccess access, String warehouseName) {
        return new CustomerWarehouseAccessResponse(access.getId(),
                access.getCustomerId(), access.getWarehouseId(), warehouseName);
    }
}
