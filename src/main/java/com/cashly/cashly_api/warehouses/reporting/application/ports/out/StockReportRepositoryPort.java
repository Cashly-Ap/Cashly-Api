package com.cashly.cashly_api.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;

import java.util.List;

public interface StockReportRepositoryPort {
    List<StockReportResponse> findAllForPdf(StockPdfQuery query);
}
