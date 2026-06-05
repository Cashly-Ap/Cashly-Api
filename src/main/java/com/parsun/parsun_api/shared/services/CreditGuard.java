package com.parsun.parsun_api.shared.services;

import com.parsun.parsun_api.receivables.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.shared.services.exceptions.CreditLimitExceededException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CreditGuard {

    private final CreditValidationService creditValidationService;

    public CreditGuard(CreditValidationService creditValidationService) {
        this.creditValidationService = creditValidationService;
    }

    /**
     * Returns the current credit status for a customer.
     */
    public CreditStatusResponse getCreditStatus(UUID customerId) {
        return creditValidationService.getCreditStatus(customerId);
    }

    /**
     * Validates that a customer has sufficient credit for a given invoice total.
     * Throws CreditLimitExceededException if credit is insufficient.
     */
    public boolean isCreditAvailable(UUID customerId, BigDecimal invoiceTotal) {
        CreditStatusResponse status = creditValidationService.getCreditStatus(customerId);
        return !status.hasOverdue()
            && status.canCreateCreditInvoice()
            && status.availableCredit().compareTo(invoiceTotal) >= 0;
    }

    public void ensureCreditAvailable(UUID customerId, BigDecimal invoiceTotal) {
        CreditStatusResponse status = creditValidationService.getCreditStatus(customerId);

        if (status.hasOverdue()) {
            throw new CreditLimitExceededException(
                    "El cliente " + status.customerName() + " tiene facturas vencidas. " +
                    "No puede crear nuevas facturas a crédito."
            );
        }

        if (!status.canCreateCreditInvoice()) {
            throw new CreditLimitExceededException(
                    "El cliente " + status.customerName() + " no puede crear facturas a crédito."
            );
        }

        if (status.availableCredit().compareTo(invoiceTotal) < 0) {
            throw new CreditLimitExceededException(
                    "Crédito insuficiente para " + status.customerName() + ". " +
                    "Disponible: " + status.availableCredit() + ", " +
                    "requerido: " + invoiceTotal
            );
        }
    }
}
