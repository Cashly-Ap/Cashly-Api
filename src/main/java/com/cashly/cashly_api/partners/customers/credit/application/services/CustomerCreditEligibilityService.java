package com.cashly.cashly_api.partners.customers.credit.application.services;

import com.cashly.cashly_api.partners.customers.credit.application.ports.out.CreditValidationGateway;
import com.cashly.cashly_api.partners.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.partners.customers.credit.application.ports.in.CustomerCreditEligibilityQuery;
import com.cashly.cashly_api.partners.customers.credit.application.ports.in.CustomerCreditLimits;
import com.cashly.cashly_api.partners.customers.credit.application.exceptions.CreditLimitExceededException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CustomerCreditEligibilityService
        implements
            CustomerCreditEligibilityQuery {

    private final CreditValidationGateway creditValidationGateway;

    public CustomerCreditEligibilityService(
            CreditValidationGateway creditValidationGateway) {
        this.creditValidationGateway = creditValidationGateway;
    }

    @Override
    public CustomerCreditLimits getCreditLimits(UUID customerId) {
        var status = getCreditStatus(customerId);
        return new CustomerCreditLimits(status.creditLimit(),
                status.availableCredit());
    }

    public CreditStatusResponse getCreditStatus(UUID customerId) {
        return creditValidationGateway.getCreditStatus(customerId);
    }

    @Override
    public boolean isCreditAvailable(UUID customerId, BigDecimal invoiceTotal) {
        CreditStatusResponse status = creditValidationGateway
                .getCreditStatus(customerId);
        return !status.hasOverdue() && status.canCreateCreditInvoice()
                && status.availableCredit().compareTo(invoiceTotal) >= 0;
    }

    public void ensureCreditAvailable(UUID customerId,
            BigDecimal invoiceTotal) {
        CreditStatusResponse status = creditValidationGateway
                .getCreditStatus(customerId);

        if (status.hasOverdue()) {
            throw new CreditLimitExceededException("El cliente "
                    + status.customerName() + " tiene facturas vencidas. "
                    + "No puede crear nuevas facturas a crédito.");
        }

        if (!status.canCreateCreditInvoice()) {
            throw new CreditLimitExceededException(
                    "El cliente " + status.customerName()
                            + " no puede crear facturas a crédito.");
        }

        if (status.availableCredit().compareTo(invoiceTotal) < 0) {
            throw new CreditLimitExceededException(
                    "Crédito insuficiente para " + status.customerName() + ". "
                            + "Disponible: " + status.availableCredit() + ", "
                            + "requerido: " + invoiceTotal);
        }
    }
}
