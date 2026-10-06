package com.cashly.cashly_api.reports.movements.application.port;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.reports.movements.application.query.MovementsPdfQuery;

import java.util.List;

public interface MovementsReportRepositoryPort {
    List<MovementReportResponse> findAllForPdf(MovementsPdfQuery query);
}
