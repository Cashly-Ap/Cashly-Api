package com.cashly.cashly_api.suppliers.application.register.request;

import com.cashly.cashly_api.suppliers.application.dto.request.AddressRequest;
import com.cashly.cashly_api.suppliers.application.dto.request.EmailRequest;
import com.cashly.cashly_api.suppliers.application.dto.request.PhoneRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CreateSupplierRequest(
    @NotBlank(message = "Legal name is required")
    String legalName,

    @NotNull(message = "Supplier type is required")
    String type,

    @NotBlank(message = "RIF is required")
    @Pattern(regexp = "^[VJGECPvjgecp]-\\d{8}-\\d$", message = "RIF format is invalid. Expected format: J-12345678-9")
    String rif,

    @NotBlank(message = "Alias is required")
    String alias,

    String notes,

    @NotNull(message = "Emails list is required")
    @Valid
    List<EmailRequest> emails,

    @NotNull(message = "Phones list is required")
    @Valid
    List<PhoneRequest> phones,

    @NotNull(message = "Addresses list is required")
    @Valid
    @NotEmpty(message = "At least one address is required")
    List<AddressRequest> addresses
) {}
