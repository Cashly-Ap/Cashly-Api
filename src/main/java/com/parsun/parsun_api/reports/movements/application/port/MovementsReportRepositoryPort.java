package com.parsun.parsun_api.reports.movements.application.port;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;
import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;

import java.util.List;

public interface MovementsReportRepositoryPort {
    List<MovementReportResponse> findAllForPdf(MovementsPdfQuery query);
}
