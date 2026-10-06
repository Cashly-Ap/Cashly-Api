package com.cashly.cashly_api.quotes.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QuoteLineDetailResponse(
        UUID id,
        UUID productId,
        String productName,
        String productCode,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        List<QuoteAllocationDetailResponse> allocations,
        List<WarehouseStockInfoResponse> warehouseStocks
) {}
