package com.parsun.parsun_api.reports.stock.application.port;

import com.parsun.parsun_api.reports.stock.application.dto.response.StockReportResponse;
import com.parsun.parsun_api.reports.stock.application.query.StockPdfQuery;

import java.util.List;

public interface StockReportRepositoryPort {
    List<StockReportResponse> findAllForPdf(StockPdfQuery query);
}
