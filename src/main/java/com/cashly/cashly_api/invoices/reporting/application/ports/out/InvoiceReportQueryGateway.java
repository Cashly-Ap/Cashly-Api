package com.cashly.cashly_api.invoices.reporting.application.ports.out;

import com.cashly.cashly_api.invoices.reporting.application.dto.InvoiceReportData;
import com.cashly.cashly_api.invoices.reporting.application.dto.InvoiceReportFilter;
import com.cashly.cashly_api.invoices.reporting.application.dto.InvoiceReportRow;
import java.util.List;
import java.util.UUID;

public interface InvoiceReportQueryGateway {
    InvoiceReportData find(UUID invoiceId);
    List<InvoiceReportRow> findAll(InvoiceReportFilter filter);
}
