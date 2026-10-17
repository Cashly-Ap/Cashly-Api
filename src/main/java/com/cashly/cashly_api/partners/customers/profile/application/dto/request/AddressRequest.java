package com.cashly.cashly_api.partners.customers.profile.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
    @NotBlank(message = "Address is required")
    String address,

    @NotNull(message = "Billing flag is required")
    Boolean billing,

    @NotNull(message = "Active flag is required")
    Boolean active,

    String state,
    String municipality,
    String postalCode
) {}
