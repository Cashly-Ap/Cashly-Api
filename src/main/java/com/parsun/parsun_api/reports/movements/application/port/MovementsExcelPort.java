package com.parsun.parsun_api.reports.movements.application.port;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;

import java.util.List;

public interface MovementsExcelPort {
    byte[] generateExcel(List<MovementReportResponse> items);
}
