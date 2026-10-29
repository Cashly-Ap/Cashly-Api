package com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;

public interface StockOverviewQueryGateway {
    StockOverviewResponse search(StockOverviewFilter filter);
}
