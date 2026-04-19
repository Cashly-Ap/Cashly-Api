package com.parsun.parsun_api.customers.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record CreateCustomerRequest(
    @NotBlank(message = "Legal name is required")
    String legalName,

    @NotBlank(message = "Customer type is required")
    String type,

    @NotBlank(message = "RIF is required")
    String rif,

    @NotBlank(message = "Alias is required")
    String alias,

    @NotNull(message = "Credit days is required")
    @PositiveOrZero(message = "Credit days must be 0 or greater")
    Integer creditDays,

    @NotNull(message = "Credit limit is required")
    @PositiveOrZero(message = "Credit limit must be 0 or greater")
    BigDecimal creditLimit,

    @NotNull(message = "Active status is required")
    Boolean active,

    @Valid
    List<EmailRequest> emails,

    @Valid
    List<PhoneRequest> phones,

    @Valid
    @NotEmpty(message = "At least one address is required")
    List<AddressRequest> addresses,

    Boolean portalAccess,

    String portalEmail
) {}
