package com.cashly.cashly_api.orders.application.convert_to_invoice;

import java.util.UUID;

public record ConvertToInvoiceResponse(
        UUID orderId,
        String orderNumber,
        String orderStatus,
        UUID invoiceId,
        String invoiceNumber
) {}
