package com.cashly.cashly_api.procurement.purchases.application.get.use_case;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway.Warehouse;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway.Stock;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseWarehouseGateway warehouseRepository;
public GetPurchaseByIdUseCase(PurchaseRepository purchaseRepository,
                                   PurchaseWarehouseGateway warehouseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.warehouseRepository = warehouseRepository;
}

    public PurchaseDetailResponse execute(UUID id) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        Map<UUID, Warehouse> allWarehouses = warehouseRepository.activeWarehouses().stream()
                .collect(Collectors.toMap(Warehouse::id, w -> w));

        Map<UUID, List<Stock>> stockByProductId = purchase.getLines().stream()
                .collect(Collectors.toMap(
                        line -> line.getProductId(),
                        line -> warehouseRepository.findByProductId(line.getProductId()),
                        (a, b) -> a
                ));

        return PurchaseMapper.toDetailResponse(purchase, allWarehouses, stockByProductId);
    }
}
