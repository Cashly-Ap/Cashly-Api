package com.cashly.cashly_api.portal.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPortalPaymentRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        UUID paymentMethodId,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        String referenceNumber,

        String notes

) {

    public boolean hasReferenceOrFile(MultipartFile justificante) {
        boolean hasReference = referenceNumber != null && !referenceNumber.isBlank();
        boolean hasFile = justificante != null && !justificante.isEmpty();
        if (!hasReference && !hasFile) {
            throw new IllegalArgumentException("Debe proporcionar una referencia o un justificante de pago");
        }
        return true;
    }
}
