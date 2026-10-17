package com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;

import java.util.List;

public interface StockPdfPort {
    byte[] generatePdf(List<StockReportResponse> items);
}
