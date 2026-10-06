package com.cashly.cashly_api.suppliers.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SupplierDetailResponse(
    String id,
    Long code,
    String legalName,
    String alias,
    String type,
    String rif,
    boolean active,
    String notes,
    LocalDateTime createdAt,
    List<EmailResponse> emails,
    List<PhoneResponse> phones,
    List<AddressResponse> addresses
) {}
