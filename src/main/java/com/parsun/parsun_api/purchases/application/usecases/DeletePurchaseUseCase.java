package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeletePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    public DeletePurchaseUseCase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        purchase.delete();
        purchaseRepository.deleteById(PurchaseId.of(id));
    }
}
