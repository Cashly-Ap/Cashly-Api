package com.cashly.cashly_api.purchases.application.usecases;


import com.cashly.cashly_api.purchases.application.ports.out.PurchaseProductGateway;


import com.cashly.cashly_api.purchases.application.dto.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class UpdatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseProductGateway productRepository;
    private final PurchaseDetailResponseAssembler responseAssembler;

    public UpdatePurchaseUseCase(PurchaseRepository purchaseRepository,
                                  PurchaseProductGateway productRepository,
                                  PurchaseDetailResponseAssembler responseAssembler) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id, UpdatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        List<PurchaseLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    PurchaseProductGateway.Entry product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    List<PurchaseLineAllocation> allocations = lineRequest.warehouseAllocations().stream()
                            .map(a -> PurchaseLineAllocation.create(a.warehouseId(), a.quantity()))
                            .toList();

                    return PurchaseLine.create(
                            product.id(),
                            product.name(),
                            product.code(),
                            lineRequest.quantity(),
                            lineRequest.unitCost(),
                            allocations
                    );
                })
                .toList();

        purchase.update(request.notes(), lines);

        Purchase saved = purchaseRepository.save(purchase);
        return responseAssembler.toDetailResponse(saved);
    }
}
