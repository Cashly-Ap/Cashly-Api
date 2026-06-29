package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

import java.util.UUID;

public class GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;
    private final WarehouseRepository warehouseRepository;

    public GetPurchaseByIdUseCase(PurchaseRepository purchaseRepository,
                                   WarehouseRepository warehouseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public PurchaseDetailResponse execute(UUID id) {
        var purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        return PurchaseMapper.toDetailResponse(purchase, warehouseRepository);
    }
}
