package com.cashly.cashly_api.reports.movements.application.ports.out;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.reports.movements.application.dto.query.MovementsPdfQuery;

import java.util.List;

public interface MovementsReportRepositoryPort {
    List<MovementReportResponse> findAllForPdf(MovementsPdfQuery query);
}
