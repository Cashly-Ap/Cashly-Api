package com.parsun.parsun_api.reports.inventory.infrastructure.persistence;

import java.math.BigDecimal;

public interface CatalogReportProjection {
    Long getProductId();
    String getProductName();
    String getSku();
    Long getCategoryId();
    String getCategoryName();
    Boolean getComposite();
    Long getWarehouseId();
    String getWarehouseName();
    BigDecimal getQuantity();
    BigDecimal getMinStock();
    Boolean getActive();
}
