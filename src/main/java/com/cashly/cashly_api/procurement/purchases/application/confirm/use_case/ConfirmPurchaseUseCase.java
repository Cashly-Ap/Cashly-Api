package com.cashly.cashly_api.procurement.purchases.application.confirm.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseActorGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.SupplierCreditGateway;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class ConfirmPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final InventoryGateway inventoryGateway;
    private final PurchaseDetailResponseAssembler responseAssembler;
    private final SupplierCreditGateway supplierCreditGateway;
    private final PurchaseActorGateway actorGateway;

    private final ActivityNotificationRecorder activityNotifications;

    public ConfirmPurchaseUseCase(PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        this(purchaseRepository, inventoryGateway, responseAssembler,
                activityNotifications, null, null);
    }

    public ConfirmPurchaseUseCase(PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications,
            SupplierCreditGateway supplierCreditGateway,
            PurchaseActorGateway actorGateway) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.purchaseRepository = purchaseRepository;
        this.inventoryGateway = inventoryGateway;
        this.responseAssembler = responseAssembler;
        this.supplierCreditGateway = supplierCreditGateway;
        this.actorGateway = actorGateway;
    }

    @Transactional
    @Audited(module = "procurement", entityType = "PURCHASE", action = "CONFIRM")
    public PurchaseDetailResponse execute(UUID id) {
        return execute(id, null, null);
    }

    @Transactional
    @Audited(module = "procurement", entityType = "PURCHASE", action = "CONFIRM")
    public PurchaseDetailResponse execute(UUID id, String confirmationPassword,
            UUID authenticatedUserId) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        authorizeCredit(purchase, confirmationPassword, authenticatedUserId);
        purchase.confirm();
        addStockFromPurchase(purchase);

        Purchase saved = purchaseRepository.save(purchase);
        activityNotifications.record(ActivityEvent.of("PURCHASES",
                "CONFIRM", "PURCHASE", saved.getId().value(),
                saved.getPurchaseNumber().value(), authenticatedUserId));
        return responseAssembler.toDetailResponse(saved);
    }

    private void authorizeCredit(Purchase purchase, String confirmationPassword,
            UUID authenticatedUserId) {
        if (purchase.getPaymentType() != PaymentType.CREDIT
                || supplierCreditGateway == null) {
            return;
        }
        boolean available = supplierCreditGateway.isCreditAvailable(
                purchase.getSupplierId(), purchase.getSubtotal());
        if (available) {
            return;
        }
        if (confirmationPassword == null || confirmationPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Se requiere contrasena para confirmar compras que exceden el limite de credito");
        }
        if (actorGateway == null || authenticatedUserId == null
                || !actorGateway.matchesPassword(authenticatedUserId,
                        confirmationPassword)) {
            throw new IllegalArgumentException("Contrasena incorrecta");
        }
    }

    private void addStockFromPurchase(Purchase purchase) {
        inventoryGateway.receive(purchase.getId().getValue(),
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
