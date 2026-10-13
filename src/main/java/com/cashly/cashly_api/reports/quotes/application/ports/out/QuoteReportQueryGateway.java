package com.cashly.cashly_api.reports.quotes.application.ports.out;

import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportData;
import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportRow;
import com.cashly.cashly_api.reports.quotes.application.dto.QuoteReportFilter;
import java.util.List;
import java.util.UUID;

public interface QuoteReportQueryGateway {
    QuoteReportData find(UUID id);
    List<QuoteReportRow> findAll(QuoteReportFilter filter);
}
