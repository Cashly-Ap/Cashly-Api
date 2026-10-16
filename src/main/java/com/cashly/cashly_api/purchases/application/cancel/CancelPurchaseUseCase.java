package com.cashly.cashly_api.purchases.application.cancel;


import com.cashly.cashly_api.purchases.application.cancel.CancelPurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.purchases.domain.Purchase;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.purchases.application.ports.out.InventoryGateway;
import java.util.UUID;

public class CancelPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final InventoryGateway inventoryGateway;
    private final PurchaseDetailResponseAssembler responseAssembler;

    public CancelPurchaseUseCase(PurchaseRepository purchaseRepository,
                                  InventoryGateway inventoryGateway,
                                  PurchaseDetailResponseAssembler responseAssembler) {
        this.purchaseRepository = purchaseRepository;
        this.inventoryGateway = inventoryGateway;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id, CancelPurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        PurchaseStatus previousStatus = purchase.getStatus();
        purchase.cancel(request.reason());
        revertStockIfConfirmed(purchase, previousStatus);

        Purchase saved = purchaseRepository.save(purchase);
        return responseAssembler.toDetailResponse(saved);
    }

    private void revertStockIfConfirmed(Purchase purchase, PurchaseStatus previousStatus) {
        if (previousStatus != PurchaseStatus.CONFIRMED) {
            return;
        }
        inventoryGateway.reverseReceipt(purchase.getId().getValue(), purchase.getPurchaseNumber().getValue(),
                purchase.getLines().stream().flatMap(line -> line.getAllocations().stream()
                        .map(allocation -> new InventoryGateway.Allocation(allocation.getWarehouseId(), line.getProductId(),
                                allocation.getQuantity().intValue()))).toList());
    }
}
