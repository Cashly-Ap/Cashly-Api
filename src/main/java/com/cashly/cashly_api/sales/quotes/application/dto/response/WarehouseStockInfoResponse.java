package com.cashly.cashly_api.sales.quotes.application.dto.response;

import java.util.UUID;

public record WarehouseStockInfoResponse(
        UUID warehouseId,
        String warehouseName,
        int available
) {}
