package com.cashly.cashly_api.customers.credit.infrastructure.integration;
import com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway;
import com.cashly.cashly_api.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.CustomerCreditQuery;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.OverdueInvoiceSnapshot;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
@Component
public class LocalCreditValidationGateway implements CreditValidationGateway {
    private final CustomerCreditQuery query;
    public LocalCreditValidationGateway(CustomerCreditQuery query) { this.query = query; }
    public CreditStatusResponse getCreditStatus(UUID customerId) {
        var snapshot = query.getCreditStatus(customerId);
        return new CreditStatusResponse(snapshot.customerId(), snapshot.customerName(), snapshot.customerRif(),
                snapshot.creditLimit(), snapshot.pendingBalance(), snapshot.customerBalance(), snapshot.availableCredit(),
                map(snapshot.overdueInvoices()), snapshot.hasOverdue(), snapshot.canCreateCreditInvoice());
    }
    public List<OverdueInvoiceResponse> findAllOverdueInvoices() { return map(query.findAllOverdueInvoices()); }
    private List<OverdueInvoiceResponse> map(List<OverdueInvoiceSnapshot> snapshots) {
        return snapshots.stream().map(entry -> new OverdueInvoiceResponse(entry.invoiceNumber(), entry.amount(), entry.dueDate(), entry.daysOverdue())).toList();
    }
}
