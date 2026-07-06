package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public GetPurchaseByIdUseCase(PurchaseRepository purchaseRepository,
                                   WarehouseRepository warehouseRepository,
                                   WarehouseStockRepository warehouseStockRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    public PurchaseDetailResponse execute(UUID id) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        Map<UUID, Warehouse> allWarehouses = warehouseRepository
                .findAllWithFilters(null, true, 0, 1000, "name", "asc")
                .content().stream()
                .collect(Collectors.toMap(Warehouse::getId, w -> w));

        Map<UUID, List<WarehouseStock>> stockByProductId = purchase.getLines().stream()
                .collect(Collectors.toMap(
                        line -> line.getProductId(),
                        line -> warehouseStockRepository.findByProductId(line.getProductId()),
                        (a, b) -> a
                ));

        return PurchaseMapper.toDetailResponse(purchase, allWarehouses, stockByProductId);
    }
}
