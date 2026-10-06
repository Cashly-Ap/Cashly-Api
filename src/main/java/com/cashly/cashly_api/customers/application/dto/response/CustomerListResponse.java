package com.cashly.cashly_api.customers.application.dto.response;

import com.cashly.cashly_api.customers.domain.valueobjects.PaymentType;

public record CustomerListResponse(
    String id,
    Long code,
    String alias,
    String legalName,
    String rif,
    String type,
    boolean active,
    PaymentType paymentType,
    boolean portalAccess,
    boolean deletable
) {}
