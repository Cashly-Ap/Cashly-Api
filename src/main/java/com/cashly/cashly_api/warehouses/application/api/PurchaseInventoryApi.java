package com.cashly.cashly_api.warehouses.application.api;

public interface PurchaseInventoryApi {
    void receive(PurchaseStockCommand command);
    void reverseReceipt(PurchaseStockCommand command);
}
