package com.cashly.cashly_api.reports.stock.application.ports.out;

import com.cashly.cashly_api.reports.stock.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.reports.stock.application.dto.query.StockPdfQuery;

import java.util.List;

public interface StockReportRepositoryPort {
    List<StockReportResponse> findAllForPdf(StockPdfQuery query);
}
