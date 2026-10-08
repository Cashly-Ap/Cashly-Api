package com.cashly.cashly_api.warehouses.application.api;

public interface ReturnInventoryApi {
    void restoreStock(ReturnStockCommand command);
    void reverseStock(ReturnStockCommand command);
}
