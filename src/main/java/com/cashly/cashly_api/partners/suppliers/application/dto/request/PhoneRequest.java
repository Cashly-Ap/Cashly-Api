package com.cashly.cashly_api.partners.suppliers.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PhoneRequest(
        @NotBlank(message = "Phone number is required") String phone) {
}
