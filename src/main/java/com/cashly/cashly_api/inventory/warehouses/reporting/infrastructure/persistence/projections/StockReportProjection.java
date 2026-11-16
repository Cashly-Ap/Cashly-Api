package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections;

public interface StockReportProjection {
    String getWarehouseName();
    String getProductName();
    String getSku();
    String getCategoryName();
    int getQuantity();
}
