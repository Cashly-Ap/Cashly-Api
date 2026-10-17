package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_stock_excel.use_case;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.StockPdfQuery;

public interface GenerateStockExcelUseCase {
    byte[] execute(StockPdfQuery query);
}
