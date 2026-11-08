package com.cashly.cashly_api.procurement.purchases.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductGateway;

import com.cashly.cashly_api.procurement.purchases.application.update.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLine;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLineAllocation;
import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class UpdatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseProductGateway productRepository;
    private final PurchaseDetailResponseAssembler responseAssembler;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdatePurchaseUseCase(PurchaseRepository purchaseRepository,
            PurchaseProductGateway productRepository,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    @Audited(module = "procurement", entityType = "PURCHASE", action = "EDIT")
    public PurchaseDetailResponse execute(UUID id,
            UpdatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        List<PurchaseLine> lines = request.lines().stream().map(lineRequest -> {
            PurchaseProductGateway.Entry product = productRepository
                    .findById(lineRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + lineRequest.productId()));

            List<PurchaseLineAllocation> allocations = lineRequest
                    .warehouseAllocations().stream()
                    .map(a -> PurchaseLineAllocation.create(a.warehouseId(),
                            a.quantity()))
                    .toList();

            return PurchaseLine.create(product.id(), product.name(),
                    product.code(), lineRequest.quantity(),
                    lineRequest.unitCost(), allocations);
        }).toList();

        purchase.update(request.notes(), lines);

        Purchase saved = purchaseRepository.save(purchase);
        activityNotifications.record(ActivityEvent.of("PURCHASES",
                "EDIT", "PURCHASE", saved.getId().value(),
                saved.getPurchaseNumber().value(), null));
        return responseAssembler.toDetailResponse(saved);
    }
}
