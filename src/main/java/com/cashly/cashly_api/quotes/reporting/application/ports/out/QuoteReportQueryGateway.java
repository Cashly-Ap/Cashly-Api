package com.cashly.cashly_api.quotes.reporting.application.ports.out;

import com.cashly.cashly_api.quotes.reporting.application.dto.model.QuoteReportData;
import com.cashly.cashly_api.quotes.reporting.application.dto.model.QuoteReportRow;
import com.cashly.cashly_api.quotes.reporting.application.dto.model.QuoteReportFilter;
import java.util.List;
import java.util.UUID;

public interface QuoteReportQueryGateway {
    QuoteReportData find(UUID id);
    List<QuoteReportRow> findAll(QuoteReportFilter filter);
}
