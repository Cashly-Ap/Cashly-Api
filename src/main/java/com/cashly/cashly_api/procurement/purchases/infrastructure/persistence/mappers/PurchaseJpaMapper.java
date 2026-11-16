package com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.mappers;

import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLine;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLineAllocation;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseLineAllocationId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseLineId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineAllocationEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseLineEntity;

import java.util.List;

public class PurchaseJpaMapper {

    private PurchaseJpaMapper() {
    }

    public static PurchaseEntity toEntity(Purchase purchase) {
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchase.getId().value());
        entity.setPurchaseNumber(purchase.getPurchaseNumber().value());
        entity.setSupplierId(purchase.getSupplierId());
        entity.setSupplierName(purchase.getSupplierName());
        entity.setSupplierRif(purchase.getSupplierRif());
        entity.setCreatedBy(purchase.getCreatedBy());
        entity.setCreatedByName(purchase.getCreatedByName());
        entity.setStatus(purchase.getStatus().name());
        entity.setPaymentType(purchase.getPaymentType() != null
                ? purchase.getPaymentType().name()
                : null);
        entity.setNotes(purchase.getNotes());
        entity.setSubtotal(purchase.getSubtotal());
        entity.setReturnedTotal(purchase.getReturnedTotal());
        entity.setReturnStatus(purchase.getReturnStatus());
        entity.setIssueDate(purchase.getIssueDate());
        entity.setDueDate(purchase.getDueDate());
        entity.setConfirmedAt(purchase.getConfirmedAt());
        entity.setCancelledAt(purchase.getCancelledAt());
        entity.setCancelledReason(purchase.getCancelledReason());
        entity.setCreatedAt(purchase.getCreatedAt());
        entity.setUpdatedAt(purchase.getUpdatedAt());

        List<PurchaseLineEntity> lineEntities = purchase.getLines().stream()
                .map(line -> toLineEntity(line, entity)).toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    public static Purchase toDomain(PurchaseEntity entity) {
        List<PurchaseLine> lines = entity.getLines().stream()
                .map(PurchaseJpaMapper::toLineDomain).toList();

        return Purchase.reconstitute(PurchaseId.of(entity.getId()),
                new PurchaseNumber(entity.getPurchaseNumber()),
                entity.getSupplierId(), entity.getSupplierName(),
                entity.getSupplierRif(), entity.getCreatedBy(),
                entity.getCreatedByName(),
                PurchaseStatus.valueOf(entity.getStatus()),
                entity.getPaymentType() != null
                        ? PaymentType.valueOf(entity.getPaymentType())
                        : null,
                entity.getIssueDate(), entity.getDueDate(),
                entity.getReturnedTotal(), entity.getReturnStatus(),
                entity.getNotes(), entity.getSubtotal(),
                entity.getConfirmedAt(), entity.getCancelledAt(),
                entity.getCancelledReason(), lines, entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private static PurchaseLineEntity toLineEntity(PurchaseLine line,
            PurchaseEntity purchaseEntity) {
        PurchaseLineEntity entity = new PurchaseLineEntity();
        entity.setId(line.getId().value());
        entity.setPurchase(purchaseEntity);
        entity.setProductId(line.getProductId());
        entity.setProductName(line.getProductName());
        entity.setProductCode(line.getProductCode());
        entity.setQuantity(line.getQuantity());
        entity.setUnitCost(line.getUnitCost());
        entity.setLineTotal(line.getLineTotal());

        List<PurchaseLineAllocationEntity> allocationEntities = line
                .getAllocations().stream()
                .map(a -> toAllocationEntity(a, entity)).toList();
        entity.getAllocations().clear();
        entity.getAllocations().addAll(allocationEntities);

        return entity;
    }

    private static PurchaseLineAllocationEntity toAllocationEntity(
            PurchaseLineAllocation allocation, PurchaseLineEntity lineEntity) {
        PurchaseLineAllocationEntity entity = new PurchaseLineAllocationEntity();
        entity.setId(allocation.getId().value());
        entity.setPurchaseLine(lineEntity);
        entity.setWarehouseId(allocation.getWarehouseId());
        entity.setQuantity(allocation.getQuantity());
        return entity;
    }

    private static PurchaseLine toLineDomain(PurchaseLineEntity entity) {
        List<PurchaseLineAllocation> allocations = entity.getAllocations()
                .stream()
                .map(a -> PurchaseLineAllocation.reconstitute(
                        PurchaseLineAllocationId.of(a.getId()),
                        a.getWarehouseId(), a.getQuantity()))
                .toList();

        return PurchaseLine.reconstitute(PurchaseLineId.of(entity.getId()),
                entity.getProductId(), entity.getProductName(),
                entity.getProductCode(), entity.getQuantity(),
                entity.getUnitCost(), entity.getLineTotal(), allocations);
    }
}
