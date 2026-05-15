package com.parsun.parsun_api.quotes.application.dto.response;

import java.util.UUID;

public record WarehouseStockInfoResponse(
        UUID warehouseId,
        String warehouseName,
        int available
) {}
