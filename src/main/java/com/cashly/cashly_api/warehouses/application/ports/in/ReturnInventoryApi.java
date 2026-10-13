package com.cashly.cashly_api.warehouses.application.ports.in;

public interface ReturnInventoryApi {
    void restoreStock(ReturnStockCommand command);
    void reverseStock(ReturnStockCommand command);
}
