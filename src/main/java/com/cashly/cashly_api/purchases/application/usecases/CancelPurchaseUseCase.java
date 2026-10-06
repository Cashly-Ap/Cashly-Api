package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.purchases.application.dto.request.CancelPurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;

    public CancelPurchaseUseCase(PurchaseRepository purchaseRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository,
                                  WarehouseRepository warehouseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id, CancelPurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        PurchaseStatus previousStatus = purchase.getStatus();
        purchase.cancel(request.reason());
        revertStockIfConfirmed(purchase, previousStatus);

        Purchase saved = purchaseRepository.save(purchase);
        return PurchaseMapper.toDetailResponse(saved, warehouseRepository);
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
