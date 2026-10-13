package com.cashly.cashly_api.reports.stock.application.usecases;

import com.cashly.cashly_api.reports.stock.application.dto.query.StockPdfQuery;

public interface GenerateStockPdfUseCase {
    byte[] execute(StockPdfQuery query);
}
