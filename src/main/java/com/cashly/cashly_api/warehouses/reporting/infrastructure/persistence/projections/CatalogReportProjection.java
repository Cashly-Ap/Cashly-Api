package com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.projections;

import java.math.BigDecimal;

public interface CatalogReportProjection {
    String getProductId();
    String getProductName();
    String getSku();
    String getCategoryId();
    String getCategoryName();
    Boolean getComposite();
    String getWarehouseId();
    String getWarehouseName();
    BigDecimal getQuantity();
    Boolean getActive();
}
