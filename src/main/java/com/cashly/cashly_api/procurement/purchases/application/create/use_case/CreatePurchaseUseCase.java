package com.cashly.cashly_api.procurement.purchases.application.create.use_case;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseSupplierGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseActorGateway;

import com.cashly.cashly_api.procurement.purchases.application.create.request.CreatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLine;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLineAllocation;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseNumber;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseSupplierGateway supplierGateway;
    private final PurchaseProductGateway productRepository;
    private final PurchaseActorGateway userRepository;
    private final PurchaseDetailResponseAssembler responseAssembler;

    public CreatePurchaseUseCase(PurchaseRepository purchaseRepository,
            PurchaseSupplierGateway supplierGateway,
            PurchaseProductGateway productRepository,
            PurchaseActorGateway userRepository,
            PurchaseDetailResponseAssembler responseAssembler) {
        this.purchaseRepository = purchaseRepository;
        this.supplierGateway = supplierGateway;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.responseAssembler = responseAssembler;
    }

    @Transactional
    public PurchaseDetailResponse execute(CreatePurchaseRequest request,
            UUID authenticatedUserId) {
        PurchaseSupplierGateway.Entry supplier = supplierGateway
                .findById(request.supplierId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier not found: " + request.supplierId()));

        PurchaseActorGateway.Entry user = userRepository
                .findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + authenticatedUserId));

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

        int year = LocalDate.now().getYear();
        int sequence = purchaseRepository.getNextSequenceForYear(year);
        PurchaseNumber purchaseNumber = PurchaseNumber.of(year, sequence);

        Purchase purchase = Purchase.create(purchaseNumber, supplier.id(),
                supplier.name(), supplier.rif(), user.id(), user.name(),
                request.notes(), lines);

        Purchase saved = purchaseRepository.save(purchase);
        return responseAssembler.toDetailResponse(saved);
    }
}
