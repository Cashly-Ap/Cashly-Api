package com.parsun.parsun_api.purchases.application.mappers;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseListResponse;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLine;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLineAllocation;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PurchaseMapper {

    private PurchaseMapper() {}

    public static PurchaseDetailResponse toDetailResponse(Purchase purchase) {
        return toDetailResponse(purchase, null);
    }

    public static PurchaseDetailResponse toDetailResponse(Purchase purchase, WarehouseRepository warehouseRepository) {
        Map<UUID, Warehouse> warehouseMap = Map.of();
        if (warehouseRepository != null && !purchase.getLines().isEmpty()) {
            warehouseMap = purchase.getLines().stream()
                    .flatMap(line -> line.getAllocations().stream())
                    .map(PurchaseLineAllocation::getWarehouseId)
                    .distinct()
                    .map(warehouseRepository::findById)
                    .filter(opt -> opt.isPresent())
                    .map(opt -> opt.get())
                    .collect(Collectors.toMap(Warehouse::getId, w -> w));
        }

        List<PurchaseDetailResponse.LineDetailResponse> lineResponses = purchase.getLines().stream()
                .map(line -> toLineDetailResponse(line, warehouseMap))
                .toList();

        return new PurchaseDetailResponse(
                purchase.getId().value(),
                purchase.getPurchaseNumber().value(),
                purchase.getSupplierId(),
                purchase.getSupplierName(),
                purchase.getSupplierRif(),
                purchase.getCreatedBy(),
                purchase.getCreatedByName(),
                purchase.getStatus().name(),
                purchase.getNotes(),
                purchase.getSubtotal(),
                purchase.getConfirmedAt(),
                purchase.getCancelledAt(),
                purchase.getCancelledReason(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt(),
                lineResponses
        );
    }

    public static PurchaseListResponse toListResponse(Purchase purchase) {
        return new PurchaseListResponse(
                purchase.getId().value(),
                purchase.getPurchaseNumber().value(),
                purchase.getSupplierId(),
                purchase.getSupplierName(),
                purchase.getStatus().name(),
                purchase.getSubtotal(),
                purchase.getLines().size(),
                purchase.getCreatedAt()
        );
    }

    private static PurchaseDetailResponse.LineDetailResponse toLineDetailResponse(
            PurchaseLine line, Map<UUID, Warehouse> warehouseMap) {
        List<PurchaseDetailResponse.LineDetailResponse.AllocationDetail> allocationDetails =
                line.getAllocations().stream()
                        .map(a -> {
                            Warehouse w = warehouseMap.get(a.getWarehouseId());
                            return new PurchaseDetailResponse.LineDetailResponse.AllocationDetail(
                                    a.getWarehouseId(),
                                    w != null ? w.getCode() : "",
                                    w != null ? w.getName() : "",
                                    a.getQuantity()
                            );
                        })
                        .toList();

        return new PurchaseDetailResponse.LineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitCost(),
                line.getLineTotal(),
                allocationDetails
        );
    }
}
