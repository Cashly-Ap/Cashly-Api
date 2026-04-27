package com.parsun.parsun_api.customers.application.dto.response;

import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerWarehouseAccessResponse(
        UUID id,
        UUID customerId,
        UUID warehouseId,
        String warehouseName,
        String warehouseCode,
        LocalDateTime grantedAt,
        UUID grantedBy
) {
    public static CustomerWarehouseAccessResponse from(CustomerWarehouseAccess access,
                                                        String warehouseName,
                                                        String warehouseCode) {
        return new CustomerWarehouseAccessResponse(
                access.getId(),
                access.getCustomerId(),
                access.getWarehouseId(),
                warehouseName,
                warehouseCode,
                access.getGrantedAt(),
                access.getGrantedBy()
        );
    }
}
