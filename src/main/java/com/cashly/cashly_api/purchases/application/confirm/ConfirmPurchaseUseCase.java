package com.cashly.cashly_api.purchases.application.confirm;


import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.purchases.domain.Purchase;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.purchases.application.ports.out.InventoryGateway;
import java.util.UUID;

public class ConfirmPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final InventoryGateway inventoryGateway;
    private final PurchaseDetailResponseAssembler responseAssembler;

    public ConfirmPurchaseUseCase(PurchaseRepository purchaseRepository,
                                   InventoryGateway inventoryGateway,
                                   PurchaseDetailResponseAssembler responseAssembler) {
        this.purchaseRepository = purchaseRepository;
        this.inventoryGateway = inventoryGateway;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        purchase.confirm();
        addStockFromPurchase(purchase);

        Purchase saved = purchaseRepository.save(purchase);
        return responseAssembler.toDetailResponse(saved);
    }

    private void addStockFromPurchase(Purchase purchase) {
        inventoryGateway.receive(purchase.getId().getValue(), purchase.getPurchaseNumber().getValue(),
                purchase.getLines().stream().flatMap(line -> line.getAllocations().stream()
                        .map(allocation -> new InventoryGateway.Allocation(allocation.getWarehouseId(), line.getProductId(),
                                allocation.getQuantity().intValue()))).toList());
    }
}
