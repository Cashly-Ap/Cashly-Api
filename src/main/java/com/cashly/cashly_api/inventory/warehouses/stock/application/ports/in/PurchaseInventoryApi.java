package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in;

public interface PurchaseInventoryApi {
    void receive(PurchaseStockCommand command);
    void reverseReceipt(PurchaseStockCommand command);
}
