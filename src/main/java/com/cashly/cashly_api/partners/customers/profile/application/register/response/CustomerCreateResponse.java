package com.cashly.cashly_api.partners.customers.profile.application.register.response;

import com.cashly.cashly_api.partners.customers.profile.application.dto.response.AddressResponse;
import com.cashly.cashly_api.partners.customers.profile.application.dto.response.EmailResponse;
import com.cashly.cashly_api.partners.customers.profile.application.dto.response.PhoneResponse;

import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerCreateResponse(String id, Long code, String legalName,
        String type, String rif, String alias, int creditDays,
        BigDecimal creditLimit, boolean active, PaymentType paymentType,
        LocalDateTime createdAt, List<EmailResponse> emails,
        List<PhoneResponse> phones, List<AddressResponse> addresses,
        String notes, UUID priceTypeId) {
}
