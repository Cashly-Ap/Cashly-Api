package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.persistence.projections;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MovementsReportProjection {
    String getMovementId();
    String getWarehouseName();
    String getProductName();
    String getSku();
    String getMovementType();
    BigDecimal getQuantity();
    BigDecimal getPreviousStock();
    BigDecimal getNewStock();
    String getNotes();
    LocalDateTime getCreatedAt();
}
