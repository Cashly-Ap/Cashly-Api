package com.parsun.parsun_api.portal.application.dto.response;

import com.parsun.parsun_api.customers.application.dto.response.AddressResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortalProfileResponse(
    UUID id,
    Long code,
    String legalName,
    String type,
    String rif,
    String alias,
    String paymentType,
    int creditDays,
    BigDecimal creditLimit,
    BigDecimal currentBalance,
    boolean active,
    boolean portalAccess,
    UUID priceTypeId,
    String notes,
    List<String> emails,
    List<String> phones,
    List<AddressResponse> addresses
) {}
