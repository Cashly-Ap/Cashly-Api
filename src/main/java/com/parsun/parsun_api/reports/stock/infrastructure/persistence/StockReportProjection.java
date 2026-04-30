package com.parsun.parsun_api.reports.stock.infrastructure.persistence;

public interface StockReportProjection {
    String getWarehouseName();
    String getProductName();
    String getSku();
    String getCategoryName();
    int getQuantity();
}
