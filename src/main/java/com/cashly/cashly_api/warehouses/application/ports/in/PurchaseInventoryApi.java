package com.cashly.cashly_api.warehouses.application.ports.in;

public interface PurchaseInventoryApi {
    void receive(PurchaseStockCommand command);
    void reverseReceipt(PurchaseStockCommand command);
}
