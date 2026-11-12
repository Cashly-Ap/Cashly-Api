package com.cashly.cashly_api.partners.suppliers.application.dto.response;

import java.math.BigDecimal;

public record SupplierCreateResponse(String id, Long code, String legalName,
        String alias, String type, String rif, int creditDays,
        BigDecimal creditLimit) {

    public SupplierCreateResponse(String id, Long code, String legalName,
            String alias, String type, String rif) {
        this(id, code, legalName, alias, type, rif, 0, BigDecimal.ZERO);
    }
}
