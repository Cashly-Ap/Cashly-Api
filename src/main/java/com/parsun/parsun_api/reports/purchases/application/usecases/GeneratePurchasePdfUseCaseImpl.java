package com.parsun.parsun_api.reports.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.reports.purchases.application.port.PurchasePdfPort;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;

import java.util.UUID;


public class GeneratePurchasePdfUseCaseImpl implements GeneratePurchasePdfUseCase {

    private final PurchaseRepository purchaseRepository;
    private final PurchasePdfPort purchasePdfPort;

    public GeneratePurchasePdfUseCaseImpl(PurchaseRepository purchaseRepository,
                                           PurchasePdfPort purchasePdfPort) {
        this.purchaseRepository = purchaseRepository;
        this.purchasePdfPort = purchasePdfPort;
    }

    @Override
    public byte[] execute(UUID purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        PurchaseDetailResponse purchaseDetail = PurchaseMapper.toDetailResponse(purchase);

        return purchasePdfPort.generatePdf(purchaseDetail);
    }
}
