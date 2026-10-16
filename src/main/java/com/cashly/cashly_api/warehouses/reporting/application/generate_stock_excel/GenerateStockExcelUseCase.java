package com.cashly.cashly_api.warehouses.reporting.application.generate_stock_excel;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;

public interface GenerateStockExcelUseCase {
    byte[] execute(StockPdfQuery query);
}
