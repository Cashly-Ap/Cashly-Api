package com.cashly.cashly_api.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;

import java.util.List;

public interface MovementsReportRepositoryPort {
    List<MovementReportResponse> findAllForPdf(MovementsPdfQuery query);
}
