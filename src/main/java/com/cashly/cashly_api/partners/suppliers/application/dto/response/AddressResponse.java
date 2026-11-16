package com.cashly.cashly_api.partners.suppliers.application.dto.response;

public record AddressResponse(String id, String address, boolean billing,
        boolean active, String state, String municipality, String postalCode) {
}
