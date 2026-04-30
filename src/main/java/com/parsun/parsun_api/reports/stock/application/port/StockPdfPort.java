package com.parsun.parsun_api.reports.stock.application.port;

import com.parsun.parsun_api.reports.stock.application.dto.response.StockReportResponse;

import java.util.List;

public interface StockPdfPort {
    byte[] generatePdf(List<StockReportResponse> items);
}
