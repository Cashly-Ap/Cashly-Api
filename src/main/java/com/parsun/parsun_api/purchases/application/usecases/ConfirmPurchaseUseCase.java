package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLine;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLineAllocation;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public ConfirmPurchaseUseCase(PurchaseRepository purchaseRepository,
                                   WarehouseStockRepository warehouseStockRepository,
                                   StockMovementRepository stockMovementRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        purchase.confirm();
        addStockFromPurchase(purchase);

        Purchase saved = purchaseRepository.save(purchase);
        return PurchaseMapper.toDetailResponse(saved);
    }

    private void addStockFromPurchase(Purchase purchase) {
        for (PurchaseLine line : purchase.getLines()) {
            for (PurchaseLineAllocation alloc : line.getAllocations()) {
                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(alloc.getWarehouseId(), line.getProductId())
                        .orElseGet(() -> WarehouseStock.create(alloc.getWarehouseId(), line.getProductId()));

                int prevQty = stock.getQuantity();
                stock.addStock(alloc.getQuantity().intValue());
                warehouseStockRepository.save(stock);

                StockMovement movement = StockMovement.createWithReference(
                        alloc.getWarehouseId(),
                        line.getProductId(),
                        MovementType.ENTRY,
                        alloc.getQuantity().intValue(),
                        prevQty,
                        stock.getQuantity(),
                        null,
                        null,
                        ReferenceType.PURCHASE,
                        purchase.getId().getValue(),
                        purchase.getPurchaseNumber().getValue(),
                        "Compra confirmada"
                );
                stockMovementRepository.save(movement);
            }
        }
    }
}
