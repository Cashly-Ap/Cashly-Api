package com.cashly.cashly_api.suppliers.application.dto.response;

public record SupplierCreateResponse(
    String id,
    Long code,
    String legalName,
    String alias,
    String type,
    String rif
) {}
