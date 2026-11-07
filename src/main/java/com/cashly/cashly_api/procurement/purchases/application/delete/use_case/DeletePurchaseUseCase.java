package com.cashly.cashly_api.procurement.purchases.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeletePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeletePurchaseUseCase(PurchaseRepository purchaseRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        purchase.delete();
        activityNotifications.record(ActivityEvent.of("PURCHASES",
                "DELETE", "PURCHASE", purchase.getId().value(),
                purchase.getPurchaseNumber().value(), null));
        purchaseRepository.deleteById(PurchaseId.of(id));
    }
}
