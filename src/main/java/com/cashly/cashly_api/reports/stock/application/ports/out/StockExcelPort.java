package com.cashly.cashly_api.reports.stock.application.ports.out;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;

import java.util.List;

public interface StockExcelPort {
    byte[] generateExcel(List<StockReportResponse> items);
}
