package com.cashly.cashly_api.partners.suppliers.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SupplierDetailResponse(String id, Long code, String legalName,
        String alias, String type, String rif, int creditDays,
        BigDecimal creditLimit, boolean active, String notes,
        LocalDateTime createdAt, List<EmailResponse> emails,
        List<PhoneResponse> phones, List<AddressResponse> addresses) {

    public SupplierDetailResponse(String id, Long code, String legalName,
            String alias, String type, String rif, boolean active, String notes,
            LocalDateTime createdAt, List<EmailResponse> emails,
            List<PhoneResponse> phones, List<AddressResponse> addresses) {
        this(id, code, legalName, alias, type, rif, 0, BigDecimal.ZERO, active,
                notes, createdAt, emails, phones, addresses);
    }
}
