package com.parsun.parsun_api.purchases.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.purchases.application.usecases.*;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseConfig {

    @Bean
    public CreatePurchaseUseCase createPurchaseUseCase(PurchaseRepository purchaseRepository,
                                                        SupplierRepository supplierRepository,
                                                        ProductRepository productRepository,
                                                        UserRepository userRepository) {
        return new CreatePurchaseUseCase(purchaseRepository, supplierRepository, productRepository, userRepository);
    }

    @Bean
    public GetPurchaseByIdUseCase getPurchaseByIdUseCase(PurchaseRepository purchaseRepository) {
        return new GetPurchaseByIdUseCase(purchaseRepository);
    }

    @Bean
    public ListPurchasesUseCase listPurchasesUseCase(PurchaseRepository purchaseRepository) {
        return new ListPurchasesUseCase(purchaseRepository);
    }

    @Bean
    public UpdatePurchaseUseCase updatePurchaseUseCase(PurchaseRepository purchaseRepository,
                                                        ProductRepository productRepository) {
        return new UpdatePurchaseUseCase(purchaseRepository, productRepository);
    }

    @Bean
    public ConfirmPurchaseUseCase confirmPurchaseUseCase(PurchaseRepository purchaseRepository,
                                                          WarehouseStockRepository warehouseStockRepository,
                                                          StockMovementRepository stockMovementRepository) {
        return new ConfirmPurchaseUseCase(purchaseRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public CancelPurchaseUseCase cancelPurchaseUseCase(PurchaseRepository purchaseRepository,
                                                        WarehouseStockRepository warehouseStockRepository,
                                                        StockMovementRepository stockMovementRepository) {
        return new CancelPurchaseUseCase(purchaseRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public DeletePurchaseUseCase deletePurchaseUseCase(PurchaseRepository purchaseRepository) {
        return new DeletePurchaseUseCase(purchaseRepository);
    }

    @Bean
    public GetProductsForPurchaseUseCase getProductsForPurchaseUseCase(ProductRepository productRepository,
                                                                        CategoryRepository categoryRepository,
                                                                        WarehouseStockRepository warehouseStockRepository,
                                                                        WarehouseRepository warehouseRepository) {
        return new GetProductsForPurchaseUseCase(productRepository, categoryRepository, warehouseStockRepository, warehouseRepository);
    }
}
