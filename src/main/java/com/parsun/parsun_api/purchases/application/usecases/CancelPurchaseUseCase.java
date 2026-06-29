package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.request.CancelPurchaseRequest;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLine;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLineAllocation;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseStatus;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public CancelPurchaseUseCase(PurchaseRepository purchaseRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id, CancelPurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        PurchaseStatus previousStatus = purchase.getStatus();
        purchase.cancel(request.reason());
        revertStockIfConfirmed(purchase, previousStatus);

        Purchase saved = purchaseRepository.save(purchase);
        return PurchaseMapper.toDetailResponse(saved);
    }

    private void revertStockIfConfirmed(Purchase purchase, PurchaseStatus previousStatus) {
        if (previousStatus != PurchaseStatus.CONFIRMED) {
            return;
        }

        for (PurchaseLine line : purchase.getLines()) {
            for (PurchaseLineAllocation alloc : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(alloc.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            int previous = stock.getQuantity();
                            stock.removeStock(alloc.getQuantity().intValue());
                            warehouseStockRepository.save(stock);

                            stockMovementRepository.save(StockMovement.createWithReference(
                                    alloc.getWarehouseId(),
                                    line.getProductId(),
                                    MovementType.CANCEL_ENTRY,
                                    alloc.getQuantity().intValue(),
                                    previous,
                                    stock.getQuantity(),
                                    null,
                                    null,
                                    ReferenceType.PURCHASE,
                                    purchase.getId().getValue(),
                                    purchase.getPurchaseNumber().getValue(),
                                    "Compra cancelada"
                            ));
                        });
            }
        }
    }
}
