package com.cashly.cashly_api.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.StockReportResponse;

import java.util.List;

public interface StockExcelPort {
    byte[] generateExcel(List<StockReportResponse> items);
}
