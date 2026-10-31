package com.cashly.cashly_api.procurement.purchases.infrastructure.config;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseWarehouseGateway;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseActorGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductCatalogGateway;

import com.cashly.cashly_api.procurement.purchases.application.cancel.use_case.CancelPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.confirm.use_case.ConfirmPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.create.use_case.CreatePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.delete.use_case.DeletePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.products.use_case.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.get.use_case.GetPurchaseByIdUseCase;
import com.cashly.cashly_api.procurement.purchases.application.list.use_case.ListPurchasesUseCase;
import com.cashly.cashly_api.procurement.purchases.application.update.use_case.UpdatePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductGateway;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseSupplierGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.InventoryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseConfig {

    @Bean
    public PurchaseDetailResponseAssembler purchaseDetailResponseAssembler(
            PurchaseWarehouseGateway warehouseGateway) {
        return new PurchaseDetailResponseAssembler(warehouseGateway);
    }

    @Bean
    public CreatePurchaseUseCase createPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            PurchaseSupplierGateway supplierGateway,
            PurchaseProductGateway productRepository,
            PurchaseActorGateway userRepository,
            PurchaseDetailResponseAssembler responseAssembler) {
        return new CreatePurchaseUseCase(purchaseRepository, supplierGateway,
                productRepository, userRepository, responseAssembler);
    }

    @Bean
    public GetPurchaseByIdUseCase getPurchaseByIdUseCase(
            PurchaseRepository purchaseRepository,
            PurchaseWarehouseGateway warehouseRepository) {
        return new GetPurchaseByIdUseCase(purchaseRepository,
                warehouseRepository);
    }

    @Bean
    public ListPurchasesUseCase listPurchasesUseCase(
            PurchaseRepository purchaseRepository) {
        return new ListPurchasesUseCase(purchaseRepository);
    }

    @Bean
    public UpdatePurchaseUseCase updatePurchaseUseCase(
            PurchaseRepository purchaseRepository,
            PurchaseProductGateway productRepository,
            PurchaseDetailResponseAssembler responseAssembler) {
        return new UpdatePurchaseUseCase(purchaseRepository, productRepository,
                responseAssembler);
    }

    @Bean
    public ConfirmPurchaseUseCase confirmPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler) {
        return new ConfirmPurchaseUseCase(purchaseRepository, inventoryGateway,
                responseAssembler);
    }

    @Bean
    public CancelPurchaseUseCase cancelPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler) {
        return new CancelPurchaseUseCase(purchaseRepository, inventoryGateway,
                responseAssembler);
    }

    @Bean
    public DeletePurchaseUseCase deletePurchaseUseCase(
            PurchaseRepository purchaseRepository) {
        return new DeletePurchaseUseCase(purchaseRepository);
    }

    @Bean
    public GetProductsForPurchaseUseCase getProductsForPurchaseUseCase(
            PurchaseProductCatalogGateway productRepository,

            PurchaseWarehouseGateway warehouseRepository) {
        return new GetProductsForPurchaseUseCase(productRepository,
                warehouseRepository);
    }
}
