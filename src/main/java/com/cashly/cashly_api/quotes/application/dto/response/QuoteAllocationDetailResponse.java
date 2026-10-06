package com.cashly.cashly_api.quotes.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteAllocationDetailResponse(
        UUID id,
        UUID warehouseId,
        String warehouseName,
        BigDecimal allocatedQuantity
) {}
