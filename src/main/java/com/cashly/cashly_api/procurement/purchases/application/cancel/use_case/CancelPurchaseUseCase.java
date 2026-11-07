package com.cashly.cashly_api.procurement.purchases.application.cancel.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.procurement.purchases.application.cancel.request.CancelPurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.InventoryGateway;
import java.util.UUID;

public class CancelPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final InventoryGateway inventoryGateway;
    private final PurchaseDetailResponseAssembler responseAssembler;

    private final ActivityNotificationRecorder activityNotifications;

    public CancelPurchaseUseCase(PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.purchaseRepository = purchaseRepository;
        this.inventoryGateway = inventoryGateway;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id,
            CancelPurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        PurchaseStatus previousStatus = purchase.getStatus();
        purchase.cancel(request.reason());
        revertStockIfConfirmed(purchase, previousStatus);

        Purchase saved = purchaseRepository.save(purchase);
        activityNotifications.record(ActivityEvent.of("PURCHASES",
                "CANCEL", "PURCHASE", saved.getId().value(),
                saved.getPurchaseNumber().value(), null));
        return responseAssembler.toDetailResponse(saved);
    }

    private void revertStockIfConfirmed(Purchase purchase,
            PurchaseStatus previousStatus) {
        if (previousStatus != PurchaseStatus.CONFIRMED) {
            return;
        }
        inventoryGateway.reverseReceipt(purchase.getId().getValue(),
                purchase.getPurchaseNumber().getValue(),
                purchase.getLines().stream().flatMap(line -> line
                        .getAllocations().stream()
                        .map(allocation -> new InventoryGateway.Allocation(
                                allocation.getWarehouseId(),
                                line.getProductId(),
                                allocation.getQuantity().intValue())))
                        .toList());
    }
}
