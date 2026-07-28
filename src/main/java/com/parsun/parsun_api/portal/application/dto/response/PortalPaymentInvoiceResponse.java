package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PortalPaymentInvoiceResponse(
    UUID invoiceId,
    String invoiceNumber,
    BigDecimal allocatedAmount
) {}
