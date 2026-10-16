package com.cashly.cashly_api.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportData;
import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportRow;
import com.cashly.cashly_api.quotes.reporting.application.dto.QuoteReportFilter;
import java.util.List;
import java.util.UUID;

public interface QuoteReportQueryGateway {
    QuoteReportData find(UUID id);
    List<QuoteReportRow> findAll(QuoteReportFilter filter);
}
