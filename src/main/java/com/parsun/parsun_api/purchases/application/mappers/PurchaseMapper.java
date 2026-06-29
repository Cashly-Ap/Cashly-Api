package com.parsun.parsun_api.purchases.application.mappers;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseListResponse;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLine;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLineAllocation;

import java.util.List;

public class PurchaseMapper {

    private PurchaseMapper() {}

    public static PurchaseDetailResponse toDetailResponse(Purchase purchase) {
        List<PurchaseDetailResponse.LineDetailResponse> lineResponses = purchase.getLines().stream()
                .map(PurchaseMapper::toLineDetailResponse)
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

    private static PurchaseDetailResponse.LineDetailResponse toLineDetailResponse(PurchaseLine line) {
        List<PurchaseDetailResponse.LineDetailResponse.AllocationDetail> allocationDetails =
                line.getAllocations().stream()
                        .map(a -> new PurchaseDetailResponse.LineDetailResponse.AllocationDetail(
                                a.getWarehouseId(),
                                a.getQuantity()
                        ))
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
