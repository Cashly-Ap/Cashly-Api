package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.purchases.application.dto.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public class UpdatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public UpdatePurchaseUseCase(PurchaseRepository purchaseRepository,
                                  ProductRepository productRepository,
                                  WarehouseRepository warehouseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
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
        return PurchaseMapper.toDetailResponse(saved, warehouseRepository);
    }
}
