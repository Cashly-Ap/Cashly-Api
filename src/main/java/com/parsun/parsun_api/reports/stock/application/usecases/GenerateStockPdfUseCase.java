package com.parsun.parsun_api.reports.stock.application.usecases;

import com.parsun.parsun_api.reports.stock.application.query.StockPdfQuery;

public interface GenerateStockPdfUseCase {
    byte[] execute(StockPdfQuery query);
}
