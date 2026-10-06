package com.cashly.cashly_api.portal.application.dto.response;

import java.util.UUID;

public record CustomerSummaryResponse(
    UUID id,
    String legalName,
    String rif,
    String alias,
    UUID priceTypeId,
    String paymentType
) {}
