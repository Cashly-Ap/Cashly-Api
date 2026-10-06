package com.cashly.cashly_api.orders.application.dto.response;

import java.util.UUID;

public record ConvertToInvoiceResponse(
        UUID orderId,
        String orderNumber,
        String orderStatus,
        UUID invoiceId,
        String invoiceNumber
) {}
