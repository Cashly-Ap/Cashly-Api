package com.cashly.cashly_api.warehouses.reporting.application.generate_stock_pdf.use_case;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.StockPdfQuery;

public interface GenerateStockPdfUseCase {
    byte[] execute(StockPdfQuery query);
}
