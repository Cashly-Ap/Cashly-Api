package com.cashly.cashly_api.reports.movements.infrastructure.persistence;

import java.time.LocalDateTime;

public interface MovementsReportProjection {
    String getMovementId();
    String getWarehouseName();
    String getProductName();
    String getSku();
    String getMovementType();
    int getQuantity();
    int getPreviousStock();
    int getNewStock();
    String getNotes();
    LocalDateTime getCreatedAt();
}
