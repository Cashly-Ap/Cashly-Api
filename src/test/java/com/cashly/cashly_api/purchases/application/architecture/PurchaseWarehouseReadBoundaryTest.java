package com.cashly.cashly_api.purchases.application.architecture;

import com.cashly.cashly_api.purchases.application.cancel.CancelPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.confirm.ConfirmPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.create.CreatePurchaseUseCase;
import com.cashly.cashly_api.purchases.application.get.GetPurchaseByIdUseCase;
import com.cashly.cashly_api.purchases.application.products.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.update.UpdatePurchaseUseCase;

import com.cashly.cashly_api.purchases.application.mappers.PurchaseMapper;
import com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PurchaseWarehouseReadBoundaryTest {
    @Test
    void shouldKeepForeignWarehouseTypesOutOfPurchaseCore() {
        for (Class<?> type : List.of(CreatePurchaseUseCase.class, UpdatePurchaseUseCase.class, ConfirmPurchaseUseCase.class,
                CancelPurchaseUseCase.class, GetPurchaseByIdUseCase.class, GetProductsForPurchaseUseCase.class,
                PurchaseMapper.class, PurchaseDetailResponseAssembler.class)) {
            for (var constructor : type.getConstructors()) {
                for (var parameter : constructor.getGenericParameterTypes()) {
                    assertFalse(parameter.getTypeName().contains("com.cashly.cashly_api.warehouses"), constructor.toString());
                }
            }
            for (var method : type.getDeclaredMethods()) {
                for (var parameter : method.getGenericParameterTypes()) {
                    assertFalse(parameter.getTypeName().contains("com.cashly.cashly_api.warehouses"), method.toString());
                }
            }
        }
    }
}
