package com.cashly.cashly_api.reports.invoices.application.ports.out;

import com.cashly.cashly_api.reports.invoices.application.dto.*;
import java.util.List;
import java.util.UUID;

public interface InvoiceReportQueryGateway {
    InvoiceReportData find(UUID invoiceId);
    List<InvoiceReportRow> findAll(InvoiceReportFilter filter);
}
