package com.cashly.cashly_api.partners.suppliers.application.dto.response;

public record SupplierListResponse(String id, Long code, String legalName,
        String alias, String type, String rif, boolean active, String createdAt,
        boolean hasPurchases) {
}
