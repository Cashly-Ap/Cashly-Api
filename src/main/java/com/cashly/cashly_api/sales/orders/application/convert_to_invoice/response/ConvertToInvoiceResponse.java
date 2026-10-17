package com.cashly.cashly_api.sales.orders.application.convert_to_invoice.response;

import java.util.UUID;

public record ConvertToInvoiceResponse(
        UUID orderId,
        String orderNumber,
        String orderStatus,
        UUID invoiceId,
        String invoiceNumber
) {}
