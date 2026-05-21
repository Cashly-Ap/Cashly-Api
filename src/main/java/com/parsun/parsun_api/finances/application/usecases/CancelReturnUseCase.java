package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.ReturnLineJpaRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class CancelReturnUseCase {

    private final ReturnRepository returnRepository;
    private final ReturnLineJpaRepository returnLineJpaRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SpringDataWarehouseStockRepository warehouseStockJpaRepository;

    public CancelReturnUseCase(ReturnRepository returnRepository,
                               ReturnLineJpaRepository returnLineJpaRepository,
                               StockMovementRepository stockMovementRepository,
                               SpringDataWarehouseStockRepository warehouseStockJpaRepository) {
        this.returnRepository = returnRepository;
        this.returnLineJpaRepository = returnLineJpaRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseStockJpaRepository = warehouseStockJpaRepository;
    }

    @Transactional
    public void execute(UUID returnId) {
        Return returnEntity = returnRepository.findById(ReturnId.of(returnId))
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        returnEntity.cancel();
        returnRepository.save(returnEntity);

        var lines = returnLineJpaRepository.findByReturnId(returnId);

        for (var lineEntity : lines) {
            var stockOpt = warehouseStockJpaRepository.findByWarehouseIdAndProductId(
                    lineEntity.getWarehouseId(), lineEntity.getProductId());

            if (stockOpt.isPresent()) {
                var stock = stockOpt.get();
                int previousStock = stock.getQuantity();
                int quantity = lineEntity.getQuantity().intValue();
                int newStock = previousStock - quantity;

                if (newStock < 0) {
                    throw new IllegalStateException("Insufficient stock to cancel return for product " +
                            lineEntity.getProductId());
                }

                stock.setQuantity(newStock);
                stock.setLastMovementAt(java.time.LocalDateTime.now());
                stock.setUpdatedAt(java.time.LocalDateTime.now());
                warehouseStockJpaRepository.save(stock);

                StockMovement movement = StockMovement.create(
                        lineEntity.getWarehouseId(),
                        lineEntity.getProductId(),
                        MovementType.EXIT,
                        quantity,
                        previousStock,
                        newStock,
                        null,
                        null,
                        "Cancelled return: " + returnEntity.getReturnNumber().value()
                );
                stockMovementRepository.save(movement);
            }
        }
    }
}
