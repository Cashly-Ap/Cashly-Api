package com.cashly.cashly_api.customers.profile.application.dto.response;

public record AddressResponse(
    String id,
    String customerId,
    String address,
    boolean billing,
    boolean active,
    String state,
    String municipality,
    String postalCode
) {}
