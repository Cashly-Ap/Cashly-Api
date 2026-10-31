package com.cashly.cashly_api.procurement.purchases.application.services;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway.Warehouse;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLineAllocation;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PurchaseDetailResponseAssembler {
    private final PurchaseWarehouseGateway warehouses;

    public PurchaseDetailResponseAssembler(
            PurchaseWarehouseGateway warehouses) {
        this.warehouses = warehouses;
    }

    public PurchaseDetailResponse toDetailResponse(Purchase purchase) {
        Map<UUID, Warehouse> warehouseDetails = purchase.getLines().stream()
                .flatMap(line -> line.getAllocations().stream())
                .map(PurchaseLineAllocation::getWarehouseId).distinct()
                .map(warehouses::findById).flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(Warehouse::id,
                        warehouse -> warehouse));
        return PurchaseMapper.toDetailResponse(purchase, warehouseDetails,
                Map.of());
    }
}
