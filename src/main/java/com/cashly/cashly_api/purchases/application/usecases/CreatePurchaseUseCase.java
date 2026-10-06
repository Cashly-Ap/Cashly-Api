package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.purchases.application.dto.request.CreatePurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.suppliers.domain.entities.Supplier;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    public CreatePurchaseUseCase(PurchaseRepository purchaseRepository,
                                  SupplierRepository supplierRepository,
                                  ProductRepository productRepository,
                                  UserRepository userRepository,
                                  WarehouseRepository warehouseRepository) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public PurchaseDetailResponse execute(CreatePurchaseRequest request, UUID authenticatedUserId) {
        Supplier supplier = supplierRepository.findById(new SupplierId(request.supplierId()))
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + request.supplierId()));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

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

        int year = LocalDate.now().getYear();
        int sequence = purchaseRepository.getNextSequenceForYear(year);
        PurchaseNumber purchaseNumber = PurchaseNumber.of(year, sequence);

        Purchase purchase = Purchase.create(
                purchaseNumber,
                supplier.getId().getValue(),
                supplier.getLegalName().getValue(),
                supplier.getRif().getValue(),
                user.getId().getValue(),
                user.getUserName().getValue(),
                request.notes(),
                lines
        );

        Purchase saved = purchaseRepository.save(purchase);
        return PurchaseMapper.toDetailResponse(saved, warehouseRepository);
    }
}
