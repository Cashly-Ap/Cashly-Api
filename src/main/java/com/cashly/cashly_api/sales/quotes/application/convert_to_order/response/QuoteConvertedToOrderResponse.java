package com.cashly.cashly_api.sales.quotes.application.convert_to_order.response;

import java.util.UUID;

public record QuoteConvertedToOrderResponse(UUID quoteId, String quoteNumber,
        UUID orderId, String orderNumber) {
}
