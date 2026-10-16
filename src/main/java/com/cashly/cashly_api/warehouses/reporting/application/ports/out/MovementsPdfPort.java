package com.cashly.cashly_api.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.MovementReportResponse;

import java.util.List;

public interface MovementsPdfPort {
    byte[] generatePdf(List<MovementReportResponse> items);
}
