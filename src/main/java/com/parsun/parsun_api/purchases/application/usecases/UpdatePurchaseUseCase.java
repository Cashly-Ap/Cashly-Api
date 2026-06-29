package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.purchases.application.dto.request.UpdatePurchaseRequest;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.application.mappers.PurchaseMapper;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLine;
import com.parsun.parsun_api.purchases.domain.entities.PurchaseLineAllocation;
import com.parsun.parsun_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.domain.valueobjects.PurchaseId;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class UpdatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public UpdatePurchaseUseCase(PurchaseRepository purchaseRepository,
                                  ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public PurchaseDetailResponse execute(UUID id, UpdatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(PurchaseId.of(id))
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        List<PurchaseLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    List<PurchaseLineAllocation> allocations = lineRequest.warehouseAllocations().stream()
                            .map(a -> PurchaseLineAllocation.create(a.warehouseId(), a.quantity()))
                            .toList();

                    return PurchaseLine.create(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
                            lineRequest.quantity(),
                            lineRequest.unitCost(),
                            allocations
                    );
                })
                .toList();

        purchase.update(request.notes(), lines);

        Purchase saved = purchaseRepository.save(purchase);
        return PurchaseMapper.toDetailResponse(saved);
    }
}
