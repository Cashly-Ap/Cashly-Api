package com.cashly.cashly_api.purchases.application.mappers;

import com.cashly.cashly_api.purchases.application.ports.out.PurchaseWarehouseGateway.Warehouse;
import com.cashly.cashly_api.purchases.application.ports.out.PurchaseWarehouseGateway.Stock;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.purchases.domain.Purchase;
import com.cashly.cashly_api.purchases.domain.PurchaseLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PurchaseMapper {

    private PurchaseMapper() {}

    public static PurchaseDetailResponse toDetailResponse(Purchase purchase) {
        return toDetailResponse(purchase, Collections.emptyMap(), Collections.emptyMap());
    }

    public static PurchaseDetailResponse toDetailResponse(Purchase purchase,
                                                           Map<UUID, Warehouse> allWarehouses,
                                                           Map<UUID, List<Stock>> stockByProductId) {
        List<PurchaseDetailResponse.LineDetailResponse> lineResponses = purchase.getLines().stream()
                .map(line -> toLineDetailResponse(line, allWarehouses, stockByProductId))
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
            PurchaseLine line,
            Map<UUID, Warehouse> allWarehouses,
            Map<UUID, List<Stock>> stockByProductId) {

        List<PurchaseDetailResponse.LineDetailResponse.AllocationDetail> allocationDetails =
                line.getAllocations().stream()
                        .map(a -> {
                            Warehouse w = allWarehouses.get(a.getWarehouseId());
                            return new PurchaseDetailResponse.LineDetailResponse.AllocationDetail(
                                    a.getWarehouseId(),
                                    w != null ? w.code() : "",
                                    w != null ? w.name() : "",
                                    a.getQuantity()
                            );
                        })
                        .toList();

        List<PurchaseDetailResponse.LineDetailResponse.StockItemDetail> stockItems =
                buildStockItems(line.getProductId(), allWarehouses, stockByProductId);

        return new PurchaseDetailResponse.LineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitCost(),
                line.getLineTotal(),
                allocationDetails,
                stockItems
        );
    }

    private static List<PurchaseDetailResponse.LineDetailResponse.StockItemDetail> buildStockItems(
            UUID productId,
            Map<UUID, Warehouse> allWarehouses,
            Map<UUID, List<Stock>> stockByProductId) {

        Map<UUID, Stock> stockByWarehouse = stockByProductId
                .getOrDefault(productId, Collections.emptyList()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Stock::warehouseId, s -> s, (a, b) -> a));

        List<PurchaseDetailResponse.LineDetailResponse.StockItemDetail> result = new ArrayList<>();
        for (Map.Entry<UUID, Warehouse> entry : allWarehouses.entrySet()) {
            Warehouse warehouse = entry.getValue();
            Stock stock = stockByWarehouse.get(entry.getKey());
            int quantity = stock != null ? stock.quantity() : 0;
            int reserved = stock != null ? stock.reserved() : 0;
            int available = stock != null ? stock.available() : 0;
            result.add(new PurchaseDetailResponse.LineDetailResponse.StockItemDetail(
                    warehouse.id(),
                    warehouse.code(),
                    warehouse.name(),
                    quantity,
                    reserved,
                    available
            ));
        }
        return result;
    }
}
