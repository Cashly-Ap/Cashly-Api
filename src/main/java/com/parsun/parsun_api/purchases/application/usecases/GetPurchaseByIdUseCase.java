package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;

import java.util.UUID;

public class GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;

    public GetPurchaseByIdUseCase(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public PurchaseDetailResponse execute(UUID id) {
        var purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        return PurchaseMapper.toDetailResponse(purchase);
    }
}
