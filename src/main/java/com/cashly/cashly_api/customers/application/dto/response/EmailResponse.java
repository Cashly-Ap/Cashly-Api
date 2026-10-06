package com.cashly.cashly_api.customers.application.dto.response;

public record EmailResponse(
    String id,
    String customerId,
    String email
) {}
