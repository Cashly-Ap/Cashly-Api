package com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportData;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportFilter;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportRow;
import java.util.List;
import java.util.UUID;

public interface InvoiceReportQueryGateway {
    InvoiceReportData find(UUID invoiceId);
    List<InvoiceReportRow> findAll(InvoiceReportFilter filter);
}
