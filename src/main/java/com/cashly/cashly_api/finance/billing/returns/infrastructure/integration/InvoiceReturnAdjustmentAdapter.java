package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceReturnCommand;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvoiceReturnAdjustmentAdapter
        implements
            ReturnInvoiceAdjustmentGateway {
    private final InvoiceReturnAdjustments adjustments;

    public InvoiceReturnAdjustmentAdapter(
            InvoiceReturnAdjustments adjustments) {
        this.adjustments = adjustments;
    }

    @Override
    public void apply(Return returned) {
        adjustments.apply(command(returned));
    }

    @Override
    public void reverse(Return returned, BigDecimal totalPaid,
            BigDecimal totalDebitNotes, BigDecimal totalCreditNotes) {
        adjustments.reverse(command(returned), totalPaid, totalDebitNotes,
                totalCreditNotes);
    }

    private InvoiceReturnCommand command(Return returned) {
        return new InvoiceReturnCommand(returned.getInvoiceId(),
                returned.getTotal(),
                returned.getLines().stream()
                        .map(line -> new InvoiceReturnCommand.Line(
                                line.getInvoiceLineId(), line.getQuantity()))
                        .toList(),
                returned.getReturnType() == ReturnType.AUTOMATIC);
    }
}
