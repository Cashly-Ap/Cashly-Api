package com.parsun.parsun_api.portal.application.dto.response;

import java.util.UUID;

public record PortalAddressResponse(
    UUID id,
    String address,
    String state,
    String municipality,
    String postalCode,
    boolean billing
) {}
