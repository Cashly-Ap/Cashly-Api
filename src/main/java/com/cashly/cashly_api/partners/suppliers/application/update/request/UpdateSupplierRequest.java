package com.cashly.cashly_api.partners.suppliers.application.update.request;

import com.cashly.cashly_api.partners.suppliers.application.dto.request.AddressRequest;
import com.cashly.cashly_api.partners.suppliers.application.dto.request.EmailRequest;
import com.cashly.cashly_api.partners.suppliers.application.dto.request.PhoneRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UpdateSupplierRequest(
        @NotBlank(message = "Legal name is required") String legalName,

        @NotBlank(message = "Supplier type is required") String type,

        @NotBlank(message = "RIF is required") @Pattern(regexp = "^[VJGECPvjgecp]-\\d{8}-\\d$", message = "RIF format is invalid. Expected format: J-12345678-9") String rif,

        @NotBlank(message = "Alias is required") String alias,

        Integer creditDays,

        java.math.BigDecimal creditLimit,

        @NotNull(message = "Active status is required") Boolean active,

        String notes,

        @Valid @NotEmpty(message = "At least one email is required") List<EmailRequest> emails,

        @Valid @NotEmpty(message = "At least one phone is required") List<PhoneRequest> phones,

        @Valid @NotEmpty(message = "At least one address is required") List<AddressRequest> addresses) {

    public UpdateSupplierRequest(String legalName, String type, String rif,
            String alias, Boolean active, String notes,
            List<EmailRequest> emails, List<PhoneRequest> phones,
            List<AddressRequest> addresses) {
        this(legalName, type, rif, alias, null, null, active, notes, emails,
                phones, addresses);
    }
}
