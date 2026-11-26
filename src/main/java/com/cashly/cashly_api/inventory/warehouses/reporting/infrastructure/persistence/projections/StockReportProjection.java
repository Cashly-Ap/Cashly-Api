package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections;

import java.math.BigDecimal;

public interface StockReportProjection {
    String getWarehouseName();
    String getProductName();
    String getSku();
    String getCategoryName();
    BigDecimal getQuantity();
}
