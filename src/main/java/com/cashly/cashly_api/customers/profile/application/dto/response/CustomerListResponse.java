package com.cashly.cashly_api.customers.profile.application.dto.response;

import com.cashly.cashly_api.customers.profile.domain.valueobjects.PaymentType;

public record CustomerListResponse(
    String id,
    Long code,
    String alias,
    String legalName,
    String rif,
    String type,
    boolean active,
    PaymentType paymentType,
    boolean deletable
) {}
