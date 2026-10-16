package com.cashly.cashly_api.warehouses.stock.application.ports.in;

public interface PurchaseInventoryApi {
    void receive(PurchaseStockCommand command);
    void reverseReceipt(PurchaseStockCommand command);
}
