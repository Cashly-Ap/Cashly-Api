package com.parsun.parsun_api.customers.application.dto.response;

import com.parsun.parsun_api.customers.domain.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerDetailResponse(
    String id,
    Long code,
    String legalName,
    String type,
    String rif,
    String alias,
    int creditDays,
    BigDecimal creditLimit,
    boolean active,
    PaymentType paymentType,
    LocalDateTime createdAt,
    List<EmailResponse> emails,
    List<PhoneResponse> phones,
    List<AddressResponse> addresses,
    boolean portalAccess,
    String notes,
    UUID priceTypeId
) {}
