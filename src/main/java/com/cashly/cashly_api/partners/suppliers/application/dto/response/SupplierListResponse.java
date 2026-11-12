package com.cashly.cashly_api.partners.suppliers.application.dto.response;

import java.math.BigDecimal;

public record SupplierListResponse(String id, Long code, String legalName,
        String alias, String type, String rif, int creditDays,
        BigDecimal creditLimit, boolean active, String createdAt,
        boolean hasPurchases) {

    public SupplierListResponse(String id, Long code, String legalName,
            String alias, String type, String rif, boolean active,
            String createdAt, boolean hasPurchases) {
        this(id, code, legalName, alias, type, rif, 0, BigDecimal.ZERO,
                active, createdAt, hasPurchases);
    }
}
