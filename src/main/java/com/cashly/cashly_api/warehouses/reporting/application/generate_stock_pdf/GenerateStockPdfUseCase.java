package com.cashly.cashly_api.warehouses.reporting.application.generate_stock_pdf;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;

public interface GenerateStockPdfUseCase {
    byte[] execute(StockPdfQuery query);
}
