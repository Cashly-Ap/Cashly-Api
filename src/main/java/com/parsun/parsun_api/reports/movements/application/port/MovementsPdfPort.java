package com.parsun.parsun_api.reports.movements.application.port;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;

import java.util.List;

public interface MovementsPdfPort {
    byte[] generatePdf(List<MovementReportResponse> items);
}
