package com.parsun.parsun_api.customers.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerCreateResponse(
    String id,
    Long code,
    String legalName,
    String type,
    String rif,
    String alias,
    int creditDays,
    BigDecimal creditLimit,
    boolean active,
    LocalDateTime createdAt,
    List<EmailResponse> emails,
    List<PhoneResponse> phones,
    List<AddressResponse> addresses
) {}
