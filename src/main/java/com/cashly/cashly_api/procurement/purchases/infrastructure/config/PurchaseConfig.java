package com.cashly.cashly_api.procurement.purchases.infrastructure.config;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
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
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchasePaymentQuery;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseActorGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.SupplierCreditGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseCashRegisterGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseConfig {

    @Bean
    public PurchaseDetailResponseAssembler purchaseDetailResponseAssembler(
            PurchaseWarehouseGateway warehouseGateway,
            PurchasePaymentQuery paymentQuery) {
        return new PurchaseDetailResponseAssembler(warehouseGateway,
                paymentQuery);
    }

    @Bean
    public CreatePurchaseUseCase createPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            PurchaseSupplierGateway supplierGateway,
            PurchaseProductGateway productRepository,
            PurchaseActorGateway userRepository,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        return new CreatePurchaseUseCase(purchaseRepository, supplierGateway,
                productRepository, userRepository, responseAssembler, activityNotifications);
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
            PurchaseSupplierGateway supplierGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        return new UpdatePurchaseUseCase(purchaseRepository, productRepository,
                supplierGateway, responseAssembler, activityNotifications);
    }

    @Bean
    public ConfirmPurchaseUseCase confirmPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications,
            SupplierCreditGateway supplierCreditGateway,
            PurchaseActorGateway actorGateway,
            PurchaseCashRegisterGateway purchaseCashRegisterGateway) {
        return new ConfirmPurchaseUseCase(purchaseRepository, inventoryGateway,
                responseAssembler, activityNotifications,
                supplierCreditGateway, actorGateway, purchaseCashRegisterGateway);
    }

    @Bean
    public CancelPurchaseUseCase cancelPurchaseUseCase(
            PurchaseRepository purchaseRepository,
            InventoryGateway inventoryGateway,
            PurchaseDetailResponseAssembler responseAssembler,
            ActivityNotificationRecorder activityNotifications) {
        return new CancelPurchaseUseCase(purchaseRepository, inventoryGateway,
                responseAssembler, activityNotifications);
    }

    @Bean
    public DeletePurchaseUseCase deletePurchaseUseCase(
            PurchaseRepository purchaseRepository,
            ActivityNotificationRecorder activityNotifications) {
        return new DeletePurchaseUseCase(purchaseRepository, activityNotifications);
    }

    @Bean
    public GetProductsForPurchaseUseCase getProductsForPurchaseUseCase(
            PurchaseProductCatalogGateway productRepository,

            PurchaseWarehouseGateway warehouseRepository) {
        return new GetProductsForPurchaseUseCase(productRepository,
                warehouseRepository);
    }
}
