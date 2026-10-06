package com.cashly.cashly_api.quotes.application.dto.response;

import java.util.UUID;

public record QuoteConvertedToOrderResponse(
        UUID quoteId,
        String quoteNumber,
        UUID orderId,
        String orderNumber
) {}
