package com.cashly.cashly_api.reports.movements.application.ports.out;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;

import java.util.List;

public interface MovementsPdfPort {
    byte[] generatePdf(List<MovementReportResponse> items);
}
