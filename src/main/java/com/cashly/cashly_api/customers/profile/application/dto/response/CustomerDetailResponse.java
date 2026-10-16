package com.cashly.cashly_api.customers.profile.application.dto.response;

import com.cashly.cashly_api.customers.profile.domain.valueobjects.PaymentType;

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
    String notes,
    UUID priceTypeId,
    boolean deletable
) {}
